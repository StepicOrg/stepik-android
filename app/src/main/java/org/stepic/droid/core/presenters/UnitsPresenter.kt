package org.stepic.droid.core.presenters

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.UnitsView
import org.stepic.droid.exceptions.UnitStoredButLessonNotException
import org.stepic.droid.model.Lesson
import org.stepic.droid.model.Progress
import org.stepic.droid.model.Section
import org.stepic.droid.model.Unit
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.ProgressUtil
import org.stepic.droid.util.StepikLogicHelper
import org.stepic.droid.web.Api
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean

class UnitsPresenter(
        private val analytic: Analytic,
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val databaseFacade: DatabaseFacade,
        private val api: Api) : PresenterBase<UnitsView>() {

    private val unitList: MutableList<Unit> = ArrayList()
    private val lessonList: MutableList<Lesson> = ArrayList()
    private val isLoading: AtomicBoolean = AtomicBoolean(false)
    private val progressMap: HashMap<Long, Progress> = HashMap()

    fun showUnits(section: Section?, isRefreshing: Boolean) {
        if (section == null) {
            view?.onEmptyUnits()
            return
        }

        if (unitList.isNotEmpty() && lessonList.isNotEmpty() && !isRefreshing) {
            view?.onNeedShowUnits(unitList, lessonList, progressMap)
        }

        if (!isLoading.compareAndSet(/* expect */ false, true)) return //if false -> set true and return true, if true -> return false
        if (!isRefreshing) {
            view?.onLoading()
        }
        threadPoolExecutor.execute {
            try {
                //go to database, it is needed always, because of is_loading, is_cached, todo: fix it.
                val cacheUnitMap = HashMap<Long, Unit>()
                val cacheLessonMap = HashMap<Long, Lesson>()
                val fromCacheUnits = databaseFacade.getAllUnitsOfSection(section.id).filterNotNull()
                if (fromCacheUnits.isNotEmpty()) {
                    val fromCacheLessons = ArrayList<Lesson>()
                    val unitProgressMapLocal = HashMap<Long, Progress>()
                    for (unit in fromCacheUnits) {
                        val progressId = unit.getProgressId()
                        if (progressId != null) {
                            val progress = databaseFacade.getProgressById(progressId)
                            unit.is_viewed_custom = progress?.is_passed ?: false
                            if (progress != null) {
                                unitProgressMapLocal.put(unit.id, progress)
                            }
                        }
                    }

                    sortUnitsByPosition(fromCacheUnits)

                    //lessons will sort automatically
                    for (unitItem in fromCacheUnits) {
                        val lesson = databaseFacade.getLessonOfUnit(unitItem) ?: throw UnitStoredButLessonNotException()
                        fromCacheLessons.add(lesson)
                    }

                    progressMap.clear()
                    progressMap.putAll(unitProgressMapLocal)
                    unitList.clear()
                    unitList.addAll(fromCacheUnits)
                    lessonList.clear()
                    lessonList.addAll(fromCacheLessons)
                    cacheLessonMap.clear()
                    cacheLessonMap.putAll(lessonList.associateBy(Lesson::id))
                    cacheUnitMap.clear()
                    cacheUnitMap.putAll(unitList.associateBy(Unit::id))
                    if (!isRefreshing) {
                        mainHandler.post {
                            view?.onNeedShowUnits(unitList, lessonList, progressMap)
                        }
                    }
                }

                // now try to update and show the data

                val unitIds = section.units
                if (unitIds == null || unitIds.isEmpty()) {
                    mainHandler.post {
                        view?.onEmptyUnits()
                    }
                } else {
                    try {
                        val backgroundUnits = ArrayList<Unit>()
                        var pointer = 0
                        while (pointer < unitIds.size) {
                            val lastExclusive = Math.min(unitIds.size, pointer + AppConstants.DEFAULT_NUMBER_IDS_IN_QUERY)
                            val subArrayForLoading = Arrays.copyOfRange(unitIds, pointer, lastExclusive)
                            val unitResponse = api.getUnits(subArrayForLoading).execute()
                            if (!unitResponse.isSuccessful) {
                                throw Exception("units is not gotten")
                            } else {
                                backgroundUnits.addAll(unitResponse.body().units)
                                pointer = lastExclusive
                            }
                        }

                        sortUnitsByPosition(backgroundUnits)

                        val backgroundLessons = ArrayList<Lesson>()
                        val lessonIds = StepikLogicHelper.fromUnitsToLessonIds(backgroundUnits)
                        pointer = 0
                        while (pointer < lessonIds.size) {
                            val lastExclusive = Math.min(lessonIds.size, pointer + AppConstants.DEFAULT_NUMBER_IDS_IN_QUERY)
                            val subArrayForLoading = Arrays.copyOfRange(lessonIds, pointer, lastExclusive)
                            val lessonsResponse = api.getLessons(subArrayForLoading).execute()
                            if (!lessonsResponse.isSuccessful) {
                                throw Exception("lesson is not gotten")
                            } else {
                                backgroundLessons.addAll(lessonsResponse.body().lessons)
                                pointer = lastExclusive
                            }
                        }

                        val idLessonMap: Map<Long, Lesson> = backgroundLessons.associateBy(Lesson::id)
                        backgroundLessons.clear()
                        backgroundUnits.forEach { unit ->
                            idLessonMap[unit.lesson]?.let {
                                backgroundLessons.add(it)
                            }
                        }


                        val backgroundProgress = ArrayList<Progress>()
                        val progressIds = ProgressUtil.getAllProgresses(backgroundUnits)
                        pointer = 0
                        while (pointer < progressIds.size) {
                            val lastExclusive = Math.min(progressIds.size, pointer + AppConstants.DEFAULT_NUMBER_IDS_IN_QUERY)
                            val subArrayForLoading = Arrays.copyOfRange<String>(progressIds, pointer, lastExclusive)
                            val progressesResponse = api.getProgresses(subArrayForLoading).execute()
                            if (!progressesResponse.isSuccessful) {
                                throw Exception("progress is not gotten")
                            } else {
                                backgroundProgress.addAll(progressesResponse.body().getProgresses())
                                pointer = lastExclusive
                            }
                        }

                        progressMap.clear()
                        val progressIdToProgress = backgroundProgress.associateBy(Progress::id)
                        backgroundUnits.forEach { unit ->
                            progressIdToProgress[unit.progress]?.let { progress ->
                                progressMap[unit.id] = progress
                            }
                        }


                        for (item in backgroundProgress) {
                            databaseFacade.addProgress(item)
                        }

                        for (unitItem in backgroundUnits) {
                            unitItem.is_viewed_custom = progressMap[unitItem.id]?.is_passed ?: false
                            databaseFacade.addUnit(unitItem)
                        }

                        for (lessonItem in backgroundLessons) {
                            databaseFacade.addLesson(lessonItem)
                            val cachedLesson = cacheLessonMap[lessonItem.id]
                            lessonItem.is_loading = cachedLesson?.is_loading ?: false
                            lessonItem.is_cached = cachedLesson?.is_cached ?: false
                        }


                        unitList.clear()
                        unitList.addAll(backgroundUnits)

                        lessonList.clear()
                        lessonList.addAll(backgroundLessons)

                        mainHandler.post {
                            view?.onNeedShowUnits(unitList, lessonList, progressMap)
                        }

                    } catch (exception: Exception) {
                        mainHandler.post {
                            analytic.reportError(Analytic.Error.UNITS_LOADING_FAIL, exception)
                            view?.onConnectionProblem()
                        }
                    }
                }

            } finally {
                isLoading.set(false)
            }
        }


    }

    private fun sortUnitsByPosition(fromCacheUnits: List<Unit>) {
        Collections.sort<Unit>(fromCacheUnits, Comparator { lhs, rhs ->
            if (lhs == null || rhs == null) {
                return@Comparator 0
            }
            val lhsPos = lhs.position
            val rhsPos = rhs.position
            return@Comparator lhsPos - rhsPos
        })
    }

}
