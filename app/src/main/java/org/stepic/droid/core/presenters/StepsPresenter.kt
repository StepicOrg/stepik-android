package org.stepic.droid.core.presenters

import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.core.presenters.contracts.StepsView
import org.stepic.droid.model.Lesson
import org.stepic.droid.model.Step
import org.stepic.droid.model.Unit
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.util.ProgressUtil
import org.stepic.droid.web.IApi
import org.stepic.droid.web.LessonStepicResponse
import org.stepic.droid.web.StepResponse
import retrofit.Response
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean

class StepsPresenter(val threadPoolExecutor: ThreadPoolExecutor,
                     val mainHandler: IMainHandler,
                     val databaseFacade: DatabaseFacade,
                     val api: IApi) : PresenterBase<StepsView>() {

    var lesson: Lesson? = null
        private set

    var isLoading = AtomicBoolean(false)

    var unit: Unit? = null

    val stepList = ArrayList<Step>()


    fun init(outLesson: Lesson?, outUnit: Unit?, simpleLessonId: Long, simpleUnitId: Long, defaultStepPositionStartWithOne: Long = -1, fromPreviousLesson: Boolean = false) {
        if (isLoading.get()) {
            return
        }

        if (this.lesson != null) {
            //already loaded if THIS.Lesson != null -> show
            view?.onLessonUnitPrepared(lesson, unit)
            view?.showSteps(fromPreviousLesson, defaultStepPositionStartWithOne)
            return
        }

        isLoading.set(true)
        view?.onLoading()
        lesson = outLesson
        unit = outUnit
        threadPoolExecutor.execute {
            try {
                if (lesson == null) {
                    initUnitLessonWithIds(simpleLessonId, simpleUnitId)
                }

                //after that Lesson should be not null
                if (lesson == null) {
                    return@execute
                }
                mainHandler.post {
                    view?.onLessonUnitPrepared(lesson, unit)
                }

                loadSteps(defaultStepPositionStartWithOne, fromPreviousLesson)

            } finally {
                isLoading.set(false)
            }
        }
    }

    private fun loadSteps(defaultStepPositionStartWithOne: Long, fromPreviousLesson: Boolean) {
        lesson?.let {
            val stepList: MutableList<Step> = databaseFacade.getStepsOfLesson(it.id).filterNotNull().toMutableList()
            stepList.sortWith(Comparator { lhs, rhs ->
                if (lhs == null || rhs == null) {
                    0.toInt()
                } else {
                    val lhsPos = lhs.position
                    val rhsPos = rhs.position
                    (lhsPos - rhsPos).toInt()
                }
            })

            var isStepsShown = false
            if (stepList.isNotEmpty() && it.steps?.size ?: -1 == stepList.size) {
                stepList.forEach {
                    it.is_custom_passed = databaseFacade.isStepPassed(it)
                }
                isStepsShown = true
                mainHandler.post {
                    this.stepList.clear()
                    this.stepList.addAll(stepList)
                    view?.showSteps(fromPreviousLesson, defaultStepPositionStartWithOne)
                }
            }

            // and try to update from internet
            var response: Response<StepResponse>? = null
            try {
                response = api.getStepsByLessonId(it.id).execute()
            } catch (ex: Exception) {
                if (!isStepsShown) {
                    mainHandler.post {
                        view?.onConnectionProblem()
                    }
                    return
                }
            }
            if (response == null) {
                if (!isStepsShown) {
                    mainHandler.post {
                        view?.onConnectionProblem()
                    }
                }
                return
            } else {
                val stepListFromInternet = response.body().steps
                if (stepListFromInternet.isEmpty()) {
                    mainHandler.post {
                        view?.onEmptySteps()
                    }
                    return
                } else {
                    updateAssignmentsAndProgresses(stepListFromInternet, unit)
                    mainHandler.post {
                        this.stepList.clear()
                        this.stepList.addAll(stepListFromInternet)
                        view?.showSteps(fromPreviousLesson, defaultStepPositionStartWithOne)
                    }
                }
            }
        }
    }

    fun refreshWhenOnConnectionProblem(outLesson: Lesson?, outUnit: Unit?, simpleLessonId: Long, simpleUnitId: Long, defaultStepPositionStartWithOne: Long = -1, fromPreviousLesson: Boolean = false) {
        if (isLoading.get()) {
            return
        }

        if (lesson == null) {
            init(outLesson, outUnit, simpleLessonId, simpleUnitId, defaultStepPositionStartWithOne, fromPreviousLesson)

        } else {
            isLoading.set(true)
            view?.onLoading()
            threadPoolExecutor.execute {
                try {
                    loadSteps(defaultStepPositionStartWithOne, fromPreviousLesson)
                } finally {
                    isLoading.set(false)
                }
            }
        }

    }

    private fun updateAssignmentsAndProgresses(stepListFromInternet: List<Step>, unit: Unit?) {
        try {
            val progressIds: Array<out String?>
            if (unit != null) {
                val assignments = api.getAssignments(unit.assignments).execute().body().assignments
                assignments.filterNotNull().forEach {
                    databaseFacade.addAssignment(assignment = it)
                }
                progressIds = ProgressUtil.getAllProgresses(assignments)
            } else {
                progressIds = ProgressUtil.getAllProgresses(stepListFromInternet)
            }


            val progresses = api.getProgresses(progressIds).execute().body().progresses
            progresses.filterNotNull().forEach {
                databaseFacade.addProgress(progress = it)
            }

            //FIXME: Warning, it is mutable objects, which we show on StepsFragment and change here or not show, if we shown from database
            stepListFromInternet.forEach {
                it.is_custom_passed = databaseFacade.isStepPassed(it)
                databaseFacade.addStep(it) // update step in db
            }
        } catch (exception: Exception) {
            //we already show steps, and we don't need onConnectionError
            //just return
            return
        }


    }

    private fun initUnitLessonWithIds(simpleLessonId: Long, simpleUnitId: Long) {
        if (simpleLessonId < 0) {
            mainHandler.post {
                view?.onLessonCorrupted()
            }
            return
        }

        lesson = databaseFacade.getLessonById(simpleLessonId)
        if (lesson == null) {
            //not in database yet
            val response: Response<LessonStepicResponse?>?
            try {
                response = api.getLessons(longArrayOf(simpleLessonId)).execute()
            } catch (ex: Exception) {
                mainHandler.post {
                    view?.onConnectionProblem()
                }
                return
            }

            try {
                lesson = response?.body()?.lessons?.firstOrNull()
                if (lesson == null) {
                    mainHandler.post {
                        view?.onLessonCorrupted()
                    }
                    return
                } else {
                    lesson?.let {
                        databaseFacade.addLesson(it)
                    }
                }
            } catch (ex: Exception) {
                mainHandler.post {
                    view?.onLessonCorrupted()
                }
                return
            }
        }

        //now lesson is parsed. Try to parse optional unit

        if (simpleUnitId >= 0) {
            unit = databaseFacade.getUnitById(simpleUnitId)
        }

        if (unit == null) {
            if (simpleUnitId >= 0) {
                //get by unitId
                try {
                    unit = api.getUnits(longArrayOf(simpleUnitId)).execute()?.body()?.units?.firstOrNull()
                } catch (ignored: Exception) {
                    // unit can be null for lesson, which is not in Course
                }
                if (!(unit?.lesson?.equals(simpleLessonId) ?: false)) {
                    //if lesson is not equal unit.lesson or something null
                    loadUnitByLessonId(simpleLessonId)
                }
            } else {
                loadUnitByLessonId(simpleLessonId)
            }

            if (!(unit?.lesson?.equals(simpleLessonId) ?: false)) {
                unit = null
            }
        }
    }

    private fun loadUnitByLessonId(simpleLessonId: Long) {
        try {
            unit = api.getUnitByLessonId(simpleLessonId).execute()?.body()?.units?.firstOrNull()
        } catch (ignored: Exception) {
            // unit can be null for lesson, which is not in Course
        }
    }

}
