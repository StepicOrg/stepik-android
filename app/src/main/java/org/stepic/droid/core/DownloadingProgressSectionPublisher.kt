package org.stepic.droid.core

import android.app.DownloadManager
import android.database.Cursor
import android.support.annotation.MainThread
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.model.DownloadEntity
import org.stepic.droid.model.Unit
import org.stepic.droid.store.CancelSniffer
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import timber.log.Timber
import java.util.*

//fixme: refactor it, merge with DownloadingProgressUnitPublisher
class DownloadingProgressSectionPublisher(private val databaseFacade: DatabaseFacade,
                                          private val systemDownloadManager: DownloadManager,
                                          private val cancelSniffer: CancelSniffer,
                                          private val mainHandler: MainHandler) {
    interface DownloadingProgressCallback {
        fun onProgressChanged(sectionId: Long, newPortion: Float)
    }

    private val UPDATE_DELAY = 300

    private var thread: Thread? = null
    private var downloadingProgressCallback: DownloadingProgressCallback? = null
    private var loadingUpdater: Runnable? = null

    @MainThread
    fun subscribe(sectionIdList: List<Long>, downloadingProgressCallback: DownloadingProgressCallback) {
        this.downloadingProgressCallback = downloadingProgressCallback
        startLoadingStatusUpdater(sectionIdList)
    }

    @MainThread
    fun unsubscribe() {
        downloadingProgressCallback = null
        stopLoadingStatusUpdater()
    }

    @MainThread
    private fun stopLoadingStatusUpdater() {
        if (loadingUpdater != null) {
            thread?.interrupt()
            loadingUpdater = null
        }
    }


    @MainThread
    private fun startLoadingStatusUpdater(sectionIdList: List<Long>) {
        if (loadingUpdater != null) return
        loadingUpdater = object : Runnable {
            private val sectionIdListLocal = ArrayList<Long>()
            private val sectionIdToUnitIdsMap = HashMap<Long, LongArray>()
            private val sectionIdToLessonIdsMap = HashMap<Long, LongArray>()
            private val lessonIdToStepsMap = HashMap<Long, LongArray>()
            private var isSectionToUnitOk = false
            private var isSectionToLessonsAndStepOk = false

            init {
                sectionIdListLocal.addAll(sectionIdList)
            }

            override fun run() {
                while (!Thread.currentThread().isInterrupted) {
                    try {
                        if (!isSectionToUnitOk) {
                            for (sectionId in sectionIdListLocal) {
                                val dbSection = databaseFacade.getSectionById(sectionId) ?: break
                                val units = dbSection.units ?: break
                                sectionIdToUnitIdsMap[dbSection.id] = units
                            }
                        }
                        if (isSectionToUnitOk || checkAllElementsInListIsExistedInMap(sectionIdListLocal, sectionIdToUnitIdsMap)) {
                            isSectionToUnitOk = true
                        } else {
                            continue
                        }

                        //here sectionIdToUnitIdsMap is filled
                        if (!isSectionToLessonsAndStepOk) {
                            sectionIdToUnitIdsMap.forEach { mapPair ->
                                val units: Map<Long, Unit> = databaseFacade.getAllUnitsOfSection(mapPair.key).filterNotNull().associateBy(Unit::id)

                                val lessonIdsOfThisSection = ArrayList<Long>()
                                for (unitId in mapPair.value) {
                                    val unit = units[unitId] ?: return@forEach //if unit is not in database -> skip all iteration of publisher
                                    val lesson = databaseFacade.getLessonById(unit.lesson) ?: return@forEach
                                    val stepIds = lesson.steps ?: return@forEach
                                    lessonIdToStepsMap[lesson.id] = stepIds
                                    lessonIdsOfThisSection.add(lesson.id)
                                }
                                sectionIdToLessonIdsMap [mapPair.key] = lessonIdsOfThisSection.toLongArray()
                            }
                        }

                        if (isSectionToLessonsAndStepOk || (checkAllElementsInListIsExistedInMap(sectionIdListLocal, sectionIdToLessonIdsMap))) {
                            // if all lessons is exist -> all stepIds is exist too
                            isSectionToLessonsAndStepOk = true
                        } else {
                            continue
                        }

                        //pre calc is finished, lets publish progress

                        for (sectionId in sectionIdListLocal) {
                            val lessonIds = sectionIdToLessonIdsMap[sectionId] ?: throw IllegalStateException("lessonIds was null, but should be initialized")
                            val stepIdsOfSection = ArrayList<Long>()
                            for (lessonId in lessonIds) {
                                val stepsIdOfLesson = lessonIdToStepsMap[lessonId] ?: throw IllegalStateException("stepIds was null, but should be initialized")
                                stepsIdOfLesson.forEach {
                                    stepIdsOfSection.add(it)
                                }
                            }

                            processOneSection(sectionId, stepIdsOfSection)
                        }


//                        for (lessonId in sectionIdListLocal.keys) {
//                            val stepIds = sectionIdListLocal[lessonId]
//                            if (stepIds != null) {
//                                processOneLesson(lessonId, stepIds)
//                            }
//                        }
                        Thread.sleep(UPDATE_DELAY.toLong())
                    } catch (e: InterruptedException) {
                        return
                    }
                }
            }

            private fun processOneSection(sectionId: Long, stepIdsOfSection: ArrayList<Long>) {
                val pairCursorAndDownloading = getCursorForSteps(stepIdsOfSection) ?: return

                val cursor = pairCursorAndDownloading.first
                val entitiesMap: Map<Int, DownloadEntity> = pairCursorAndDownloading.second.associate { kotlin.Pair(it.downloadId.toInt(), it) }

                val stepIdToProgress = HashMap<Long, Float>()
                cursor.use { cursor ->
                    cursor.moveToFirst()
                    while (!cursor.isAfterLast) {
                        val bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        val bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val columnStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                        val downloadId = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_ID))

                        val relatedDownloadEntity = entitiesMap[downloadId]

                        if (relatedDownloadEntity != null && !cancelSniffer.isStepIdCanceled(relatedDownloadEntity.stepId)) {
                            if (columnStatus == DownloadManager.STATUS_SUCCESSFUL) {
                                stepIdToProgress[relatedDownloadEntity.stepId] = 1f
                            } else {
                                stepIdToProgress[relatedDownloadEntity.stepId] = bytes_downloaded.toFloat() / bytes_total
                            }
                        }

                        cursor.moveToNext()
                    }
                }

                val dbSteps = databaseFacade.getStepsById(stepIdsOfSection)
                if (dbSteps.size < stepIdsOfSection.size) {
                    return
                }

                val dbVideoSteps = dbSteps.filter { it.block?.name == AppConstants.TYPE_VIDEO }

                dbVideoSteps.forEach {
                    if (stepIdToProgress.containsKey(it.id)) {
                        // ok we have progress of it
                    } else {
                        stepIdToProgress[it.id] = if (it.is_cached) {
                            1f
                        } else {
                            0f
                        }
                    }
                }

                var partialProgress = 0f
                stepIdToProgress.forEach {
                    partialProgress += it.value
                }

                partialProgress /= stepIdToProgress.size

                val partialProgressValue = partialProgress

                mainHandler.post {
                    Timber.d("sectionId = %s, progress = %f", sectionId, partialProgressValue)
                    downloadingProgressCallback?.onProgressChanged(sectionId, partialProgressValue)
                }
            }

            private fun checkAllElementsInListIsExistedInMap(list: List<Long>, map: HashMap<Long, LongArray>): Boolean {
                list.forEach {
                    map[it] ?: return false
                }
                return true
            }


            private fun getCursorForSteps(stepIds: Iterable<Long>): Pair<Cursor, List<DownloadEntity>>? {

                val nowDownloadingListOfSpecificSteps = databaseFacade
                        .getAllDownloadEntities()
                        .filterNotNull()
                        .filter { it.stepId in stepIds }

                val ids = getAllDownloadIds(nowDownloadingListOfSpecificSteps)
                if (ids == null || ids.isEmpty()) return null

                val query = DownloadManager.Query()
                query.setFilterById(*ids)
                return Pair(systemDownloadManager.query(query), nowDownloadingListOfSpecificSteps)

            }

            private fun getAllDownloadIds(list: List<DownloadEntity>): LongArray? {
                val copyOfList = ArrayList(list)
                val result = LongArray(copyOfList.size)
                var i = 0
                for (element in copyOfList) {
                    if (!cancelSniffer.isStepIdCanceled(element.stepId))
                        result[i++] = element.downloadId
                }
                return result
            }

        }
        thread = Thread(loadingUpdater)
        thread?.start()
    }

}