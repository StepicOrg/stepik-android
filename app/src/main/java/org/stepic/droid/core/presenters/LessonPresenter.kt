package org.stepic.droid.core.presenters

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.LessonView
import org.stepic.droid.di.lesson.LessonScope
import org.stepic.droid.persistence.content.StepContentResolver
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Step
import org.stepik.android.model.Unit
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.ProgressUtil
import org.stepic.droid.web.Api
import org.stepic.droid.web.LessonStepicResponse
import org.stepic.droid.web.StepResponse
import retrofit2.Response
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@LessonScope
class LessonPresenter
@Inject
constructor(
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val databaseFacade: DatabaseFacade,
        private val api: Api,
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val analytic: Analytic,

        private val stepContentResolver: StepContentResolver
) : PresenterBase<LessonView>() {

    private var lesson: Lesson? = null

    private var isLoading = AtomicBoolean(false)

    private var unit: Unit? = null

    private var section: Section? = null

    val stepList = ArrayList<StepPersistentWrapper>()


    @JvmOverloads
    fun init(outLesson: Lesson? = null,
             outUnit: Unit? = null,
             simpleLessonId: Long = -1,
             simpleUnitId: Long = -1,
             defaultStepPositionStartWithOne: Long = -1,
             fromPreviousLesson: Boolean = false,
             section: Section? = null) {

        if (lesson != null) {
            view?.onLessonUnitPrepared(lesson, unit, this.section)
            if (this.stepList.isEmpty()) {
                if (isLoading.compareAndSet(false, true)) {
                    threadPoolExecutor.execute {
                        try {
                            loadSteps(defaultStepPositionStartWithOne, fromPreviousLesson)
                        } finally {
                            isLoading.set(false)
                        }
                    }
                }
            } else {
                view?.showSteps(fromPreviousLesson, defaultStepPositionStartWithOne)
            }
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

                if (lesson?.isPublic == false) {
                    //lesson is not public
                    val profileResponse = sharedPreferenceHelper.authResponseFromStore
                    if (profileResponse == null) {
                        mainHandler.post {
                            view?.onUserNotAuth()
                        }
                        return@execute
                    }
                }
                //after that Lesson should be not null
                if (lesson == null) {
                    return@execute
                }

                val sectionId = unit?.section ?: -1L

                if (section == null && sectionId >= 0) {
                    this.section = databaseFacade.getSectionById(sectionId)
                    if (this.section == null) {
                        try {
                            this.section = api.getSections(longArrayOf(sectionId)).execute().body()?.sections?.firstOrNull()
                            // do not add to cache section in this way, because we need to support loading/caching state :<
                        } catch (ignored: Exception) {
                            // ok, section is optional
                        }
                    }
                } else {
                    this.section = section
                }

                mainHandler.post {
                    view?.onLessonUnitPrepared(lesson, unit, this.section)
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
                    0
                } else {
                    val lhsPos = lhs.position
                    val rhsPos = rhs.position
                    (lhsPos - rhsPos).toInt()
                }
            })

            var isStepsShown = false
            if (stepList.isNotEmpty() && it.steps?.size ?: -1 == stepList.size) {
                val steps = stepList.map { step ->
                    step.isCustomPassed = databaseFacade.isStepPassed(step)
                    stepContentResolver.resolvePersistentContent(step).blockingFirst()
                }
                isStepsShown = true
                //if we get steps from database -> progresses and assignments were stored
                mainHandler.post {
                    this.stepList.clear()
                    this.stepList.addAll(steps)
                    view?.showSteps(fromPreviousLesson, defaultStepPositionStartWithOne)
                }
            }

            if (!isStepsShown && it.steps?.isEmpty() ?: true) {
                mainHandler.post {
                    view?.onEmptySteps()
                }
                return
            }

            // and try to update from internet
            var response: Response<StepResponse>? = null
            try {
                response = api.getSteps(it.steps).execute()
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
                val stepListFromInternet = response.body()?.steps?.map { step -> stepContentResolver.resolvePersistentContent(step).blockingFirst() }
                if (stepListFromInternet == null || stepListFromInternet.isEmpty()) {
                    if (!isStepsShown) {
                        if (it.steps?.isEmpty() ?: true) {
                            //lesson does not have steps
                            mainHandler.post {
                                view?.onEmptySteps()
                            }
                        } else {
                            //access is denied
                            val code = response.code().toString()
                            analytic.reportEventWithIdName(Analytic.Error.LESSON_ACCESS_DENIED, code, response.errorBody()?.string() ?: "error body was null")
                            analytic.reportEventWithIdName(Analytic.Error.LESSON_ACCESS_DENIED, code, response.message()?.toString() ?: "message  was null")
                            mainHandler.post {
                                view?.onLessonCorrupted()
                            }
                        }
                    }
                    return
                } else {
                    updateAssignmentsAndProgresses(stepListFromInternet, unit)
                    //only after getting progresses and assignments we can get steps
                    if (!isStepsShown) {
                        mainHandler.post {
                            this.stepList.clear()
                            this.stepList.addAll(stepListFromInternet)
                            view?.showSteps(fromPreviousLesson, defaultStepPositionStartWithOne)
                        }
                    }
                }
            }
        }
    }

    fun refreshWhenOnConnectionProblem(outLesson: Lesson?, outUnit: Unit?, simpleLessonId: Long, simpleUnitId: Long, defaultStepPositionStartWithOne: Long = -1, fromPreviousLesson: Boolean = false, section: Section?) {
        if (isLoading.get()) {
            return
        }

        if (lesson == null) {
            init(outLesson, outUnit, simpleLessonId, simpleUnitId, defaultStepPositionStartWithOne, fromPreviousLesson, section)

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

    private fun updateAssignmentsAndProgresses(stepListFromInternet: List<StepPersistentWrapper>, unit: Unit?) {
        try {
            val progressIds: Array<out String?>
            if (unit != null) {
                val assignments = api.getAssignments(unit.assignments).execute().body()?.assignments
                assignments?.filterNotNull()?.forEach {
                    databaseFacade.addAssignment(assignment = it)
                }
                progressIds = ProgressUtil.getProgresses(assignments)
            } else {
                progressIds = ProgressUtil.getProgresses(stepListFromInternet)
            }


            val progresses = api.getProgresses(progressIds).execute().body()?.progresses
            progresses?.filterNotNull()?.forEach {
                databaseFacade.addProgress(progress = it)
            }

            //FIXME: Warning, it is mutable objects, which we show on LessonFragment and change here or not show, if we shown from database
            stepListFromInternet.forEach {
                it.step.isCustomPassed = databaseFacade.isStepPassed(it.step)
                databaseFacade.addStep(it.step) // update step in db
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
                lesson?.let {
                    databaseFacade.addLesson(it)
                }
                if (lesson == null) {
                    mainHandler.post {
                        view?.onLessonCorrupted()
                    }
                    return
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
                //get by lessonId
                try {
                    unit = api.getUnits(listOf(simpleUnitId)).execute()?.body()?.units?.firstOrNull()
                } catch (ignored: Exception) {
                    // unit can be null for lesson, which is not in Course
                }
                if (unit?.lesson != simpleLessonId) {
                    //if lesson is not equal unit.lesson or something null
                    loadUnitByLessonId(simpleLessonId)
                }
            } else {
                loadUnitByLessonId(simpleLessonId)
            }

            if (unit?.lesson != simpleLessonId) {
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
