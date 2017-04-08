package org.stepic.droid.core

import android.app.DownloadManager
import android.database.Cursor
import android.support.annotation.MainThread
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.model.DownloadEntity
import org.stepic.droid.storage.CancelSniffer
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import java.util.*

//fixme: refactor it, merge with DownloadingProgressSectionPublisher
class DownloadingProgressUnitPublisher(private val databaseFacade: DatabaseFacade,
                                       private val systemDownloadManager: DownloadManager,
                                       private val cancelSniffer: CancelSniffer,
                                       private val mainHandler: MainHandler) {

    interface DownloadingProgressCallback {
        fun onProgressChanged(lessonId: Long, newPortion: Float)
    }

    private val UPDATE_DELAY = 300

    private var thread: Thread? = null
    private var downloadingProgressCallback: DownloadingProgressCallback? = null
    private var loadingUpdater: Runnable? = null

    @MainThread
    fun subscribe(lessonIdToStepIds: Map<Long, Set<Long>>, downloadingProgressCallback: DownloadingProgressCallback) {
        this.downloadingProgressCallback = downloadingProgressCallback
        startLoadingStatusUpdater(lessonIdToStepIds)
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
    private fun startLoadingStatusUpdater(lessonIdToStepIds: Map<Long, Set<Long>>) {
        if (loadingUpdater != null) return
        loadingUpdater = object : Runnable {
            private val lessonIdToStepIdsLocal = HashMap<Long, Set<Long>>()

            init {
                lessonIdToStepIdsLocal.putAll(lessonIdToStepIds)
            }

            override fun run() {
                while (!Thread.currentThread().isInterrupted) {
                    try {
                        for (lessonId in lessonIdToStepIdsLocal.keys) {
                            val stepIds = lessonIdToStepIdsLocal[lessonId]
                            if (stepIds != null) {
                                processOneLesson(lessonId, stepIds)
                            }
                        }
                        Thread.sleep(UPDATE_DELAY.toLong())
                    } catch (e: InterruptedException) {
                        return
                    }
                }
            }

            private fun processOneLesson(lessonId: Long, stepIds: Set<Long>) {
                val pairCursorAndDownloading = getCursorForSteps(stepIds)

                val cursor = pairCursorAndDownloading?.first
                val entitiesMap: Map<Int, DownloadEntity>? = pairCursorAndDownloading?.second?.associate { kotlin.Pair(it.downloadId.toInt(), it) }

                val stepIdToProgress = HashMap<Long, Float>()
                if (entitiesMap != null) {
                    cursor?.use { cursor ->
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
                }

                val dbSteps = databaseFacade
                        .getStepsOfLesson(lessonId)
                        .filterNotNull()
                if (dbSteps.size < stepIds.size) {
                    //not all steps of the lesson are cached -> do not post progress
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
                val isAnyStepLoading: Boolean = dbVideoSteps.find { it.is_loading } != null

                if (isAnyStepLoading && partialProgressValue > 0f) {
                    mainHandler.post {
                        downloadingProgressCallback?.onProgressChanged(lessonId, partialProgressValue)
                    }
                }

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