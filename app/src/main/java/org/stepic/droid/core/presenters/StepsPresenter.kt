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


    fun init(outLesson: Lesson?, outUnit: Unit?, simpleLessonId: Long, simpleUnitId: Long, defaultStepPositionStartWithOne: Long) {
        if (this.lesson != null) {
            //already loaded if THIS.Lesson != null
            return
        }

        if (isLoading.get()) {
            return
        }

        isLoading.set(true)
        lesson = outLesson
        unit = outUnit
        threadPoolExecutor.execute {
            try {
                if (lesson == null || unit == null) {
                    initUnitLessonWithIds(simpleLessonId, simpleUnitId)
                }

                //after that Lesson should be not null
                if (lesson == null || unit == null) {
                    return@execute
                }
                mainHandler.post {
                    view?.onLessonUnitPrepared(lesson, unit)
                }

                lesson?.let {
                    val stepList = databaseFacade.getStepsOfLesson(it.id)
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
                        isStepsShown = true
                        mainHandler.post {
                            view?.showSteps(stepList)
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
                            return@execute
                        }
                    }
                    if (response == null) {
                        if (!isStepsShown) {
                            mainHandler.post {
                                view?.onConnectionProblem()
                            }
                        }
                        return@execute
                    } else {
                        val stepListFromInternet = response.body().steps
                        if (stepListFromInternet.isEmpty()) {
                            mainHandler.post {
                                view?.onEmptySteps()
                            }
                            return@execute
                        } else {
                            //todo if shown?
                            if (!isStepsShown) {
                                isStepsShown = true
                                mainHandler.post {
                                    view?.showSteps(stepListFromInternet)
                                }
                            }

                            updateAssignmentsAndProgresses(stepListFromInternet, unit!!)
                        }
                    }
                }

            } finally {
                isLoading.set(false)
            }
        }
    }

    private fun updateAssignmentsAndProgresses(stepListFromInternet: List<Step>, unit: Unit) {
        try {
            val assignments = api.getAssignments(unit.assignments).execute().body().assignments
            assignments.filterNotNull().forEach {
                databaseFacade.addAssignment(assignment = it)
            }

            val progressIds = ProgressUtil.getAllProgresses(assignments)
            val progresses = api.getProgresses(progressIds).execute().body().progresses
            progresses.filterNotNull().forEach {
                databaseFacade.addProgress(progress = it)
            }

            //FIXME: Warning, it is mutable objects, which we show on StepsFragment and change here or not show, if we shown from database
            stepListFromInternet.forEach {
                it.is_custom_passed = databaseFacade.isStepPassed(it.id)
                databaseFacade.addStep(it) // update step in db
            }

            mainHandler.post {
                view?.updateTabState(stepListFromInternet)
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
                    mainHandler.post {
                        view?.onLessonCorrupted()
                    }
                    return
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
                mainHandler.post {
                    view?.onLessonCorrupted()
                }
                return
            }
        }
    }

    private fun loadUnitByLessonId(simpleLessonId: Long) {
        try {
            unit = api.getUnitByLessonId(simpleLessonId).execute()?.body()?.units?.firstOrNull()
        } catch (ignored: Exception) {
            mainHandler.post {
                view?.onLessonCorrupted()
            }
            return
        }
    }

}
