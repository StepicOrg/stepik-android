package org.stepic.droid.core.presenters

import android.support.annotation.MainThread
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.RouteStepView
import org.stepic.droid.di.step.StepScope
import org.stepic.droid.model.Course
import org.stepic.droid.model.Lesson
import org.stepic.droid.model.Section
import org.stepic.droid.model.Unit
import org.stepic.droid.storage.repositories.Repository
import org.stepic.droid.util.hasUserAccess
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@StepScope
class RouteStepPresenter
@Inject constructor(
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler,
        private val analytic: Analytic,
        private val courseRepository: Repository<Course>,
        private val sectionRepository: Repository<Section>,
        private val unitRepository: Repository<Unit>)
    : PresenterBase<RouteStepView>() {

    /**
     * Last step in lesson can be shown differently
     */
    @MainThread
    fun checkStepForLast(stepId: Long, lesson: Lesson, unit: Unit) {
        checkStepBase(
                Direction.next,
                stepId,
                lesson,
                unit,
                indexCalculation = { something -> something.size - 1 },
                resultForView = { view?.showNextLessonView() }) //need only last
    }

    @MainThread
    fun checkStepForFirst(stepId: Long, lesson: Lesson, unit: Unit) {
        checkStepBase(
                Direction.previous,
                stepId,
                lesson,
                unit,
                indexCalculation = { _ -> 0 },
                resultForView = { view?.showPreviousLessonView() }) //need only the first element
    }

    @MainThread
    private fun checkStepBase(direction: Direction, stepId: Long, lesson: Lesson, unit: Unit, indexCalculation: (LongArray) -> Int, resultForView: () -> kotlin.Unit) {
        val stepIds = lesson.steps
        if (stepIds == null || stepIds.isEmpty()) {
            return
        }

        val conditionalStepId = stepIds[indexCalculation.invoke(stepIds)]
        if (conditionalStepId != stepId) {
            return
        }

        //yes, step is candidate for showing
        if (direction == Direction.previous) {
            if (unit.position > 1) { //not first
                resultForView.invoke()
            } else {
                //unit.position is 1 (it is first). We should check for previous section is available or not
                threadPoolExecutor.execute {
                    val section: Section = sectionRepository.getObject(unit.section) ?: return@execute
                    if (section.position <= 1) {
                        //it is fist section in course
                        return@execute
                    }

                    //only if it is not 1st module we have a chance
                    val course: Course? = courseRepository.getObject(section.course)
                    course
                            ?.sections
                            ?.slice(0..section.position - 2)
                            ?.toLongArray()
                            ?.let { sectionIds ->
                                val sections = sectionRepository.getObjects(sectionIds)
                                //this section are previous our
                                sections
                                        .reversed()
                                        .forEach {
                                            if (it.hasUserAccess(course)) {
                                                mainHandler.post {
                                                    resultForView.invoke()
                                                }
                                                return@execute
                                            }
                                        }
                            }
                }
            }
        } else if (direction == Direction.next) {
            TODO()
        }
    }

    fun clickNextLesson(unit: Unit) {
        analytic.reportEvent(Analytic.Interaction.CLICK_NEXT_LESSON_IN_STEPS)
        clickLessonBase(unit,
                nextIndex = { index -> index + 1 },
                onOpen = { nextUnit, nextLesson -> view?.openNextLesson(nextUnit, nextLesson) },
                onCantGoAnalytic = { unit -> analytic.reportError(Analytic.Error.ILLEGAL_STATE_NEXT_LESSON, IllegalStateRouteLessonException(unit.id)) },
                onCantGoEvent = { view?.showCantGoNext() }
        )
    }


    fun clickPreviousLesson(unit: Unit) {
        analytic.reportEvent(Analytic.Interaction.CLICK_PREVIOUS_LESSON_IN_STEPS)
        clickLessonBase(unit,
                nextIndex = { index -> index - 1 },
                onOpen = { previousUnit, previousLesson -> view?.openPreviousLesson(previousUnit, previousLesson) },
                onCantGoAnalytic = { unit -> analytic.reportError(Analytic.Error.ILLEGAL_STATE_PREVIOUS_LESSON, IllegalStateRouteLessonException(unit.id)) },
                onCantGoEvent = { view?.showCantGoPrevious() }
        )
    }

    private fun clickLessonBase(unit: Unit,
                                nextIndex: (Int) -> Int,
                                onOpen: (Unit, Lesson) -> kotlin.Unit,
                                onCantGoAnalytic: (Unit) -> kotlin.Unit,
                                onCantGoEvent: () -> kotlin.Unit) {
//        view?.showLoading()
//        threadPoolExecutor.execute {
//            val section = databaseFacade.getSectionById(unit.section)
//            var nextUnitId: Long? = null
//
//            val unitIds = section?.units
//            val numberOfUnits = unitIds?.size ?: 0
//            let {
//                unitIds?.forEachIndexed { index, unitId ->
//                    if (unit.id == unitId && nextIndex.invoke(index) < numberOfUnits && nextIndex.invoke(index) >= 0) {
//                        nextUnitId = unitIds[nextIndex.invoke(index)]
//                        return@let  //alias for break
//                    }
//                }
//            }
//            if (nextUnitId != null) {
//                val nextUnit = databaseFacade.getUnitById(nextUnitId!!)
//                if (nextUnit != null) {
//                    val nextLesson = databaseFacade.getLessonById(nextUnit.lesson)
//                    if (nextLesson != null) {
//                        mainHandler.post {
//                            onOpen.invoke(nextUnit, nextLesson)
//                        }
//                        return@execute
//                    }
//                }
//            }
//
//            //if someone is null -> show error
//            onCantGoAnalytic.invoke(unit)
//            mainHandler.post {
//                onCantGoEvent.invoke()
//            }
//
//        }
    }

    inner class IllegalStateRouteLessonException(unitId: Long) : IllegalStateException("Next or previous lesson is shouldn't be shown, lessonId = $unitId")

    enum class Direction {
        previous, next
    }
}