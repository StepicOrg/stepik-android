package org.stepic.droid.core.presenters

import android.support.annotation.MainThread
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.presenters.contracts.RouteStepView
import org.stepik.android.model.Course
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Unit
import org.stepic.droid.storage.repositories.Repository
import org.stepic.droid.util.hasUserAccessAndNotEmpty
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

class RouteStepPresenter
@Inject constructor(
    private val threadPoolExecutor: ThreadPoolExecutor,
    private val mainHandler: MainHandler,
    private val analytic: Analytic,
    private val courseRepository: Repository<Course>,
    private val sectionRepository: Repository<Section>,
    private val unitRepository: Repository<Unit>,
    private val lessonRepository: Repository<Lesson>
) : PresenterBase<RouteStepView>() {

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
                resultForView = { view?.showNextLessonView() }) //need only last
    }

    @MainThread
    fun checkStepForFirst(stepId: Long, lesson: Lesson, unit: Unit) {
        checkStepBase(
                Direction.previous,
                stepId,
                lesson,
                unit,
                resultForView = { view?.showPreviousLessonView() }) //need only the first element
    }

    @MainThread
    private fun checkStepBase(direction: Direction, stepId: Long, lesson: Lesson, unit: Unit, resultForView: () -> kotlin.Unit) {
        val stepIds = lesson.steps
        if (stepIds == null || stepIds.isEmpty()) {
            return
        }

        val indexForChecking =
                when (direction) {
                    RouteStepPresenter.Direction.previous -> 0
                    RouteStepPresenter.Direction.next -> stepIds.size - 1
                }
        if (stepIds[indexForChecking] != stepId) {
            // it is not the last or the fist in the lesson
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
                    val course = courseRepository.getObject(section.course)
                    val slicedSectionIds = getSlicedSectionIds(direction, section, course)
                    slicedSectionIds
                            ?.let {
                                val sections = sectionRepository.getObjects(slicedSectionIds)
                                //this section are previous our
                                sections
                                        .reversed()
                                        .forEach {
                                            if (it.hasUserAccessAndNotEmpty(course)) {
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
            threadPoolExecutor.execute {
                val section = sectionRepository.getObject(unit.section) ?: return@execute
                val unitIds = section.units ?: return@execute
                if (unitIds[unitIds.size - 1] == unit.id) {
                    //we should check next sections with access

                    val course = courseRepository.getObject(section.course)
                    val sectionIds = getSlicedSectionIds(direction, section, course)
                    sectionIds?.let {
                        val sections = sectionRepository.getObjects(it)
                        sections
                                .forEach {
                                    if (it.hasUserAccessAndNotEmpty(course)) {
                                        mainHandler.post {
                                            resultForView.invoke()
                                        }
                                        return@execute
                                    }
                                }
                    }

                } else {
                    mainHandler.post {
                        resultForView.invoke()
                    }
                }
            }
        }
    }

    private fun getSlicedSectionIds(direction: Direction, currentSection: Section, course: Course?): LongArray? {
        val sectionIds = course?.sections

        return when (direction) {
            RouteStepPresenter.Direction.previous -> {
                sectionIds?.slice(0..currentSection.position - 2)
                        ?.toLongArray()
            }
            RouteStepPresenter.Direction.next -> {
                sectionIds
                        ?.slice(currentSection.position until sectionIds.size)
                        ?.toLongArray()
            }
        }
    }

    fun clickNextLesson(unit: Unit) {
        analytic.reportEvent(Analytic.Interaction.CLICK_NEXT_LESSON_IN_STEPS)
        clickLessonBase(
                direction = Direction.next,
                unit = unit,
                nextIndex = { index -> index + 1 },
                onOpen = { nextUnit, nextLesson, nextSection -> view?.openNextLesson(nextUnit, nextLesson, nextSection) },
                onCantGoAnalytic = { unit -> analytic.reportError(Analytic.Error.ILLEGAL_STATE_NEXT_LESSON, IllegalStateRouteLessonException(unit.id)) },
                onCantGoEvent = { view?.showCantGoNext() }
        )
    }


    fun clickPreviousLesson(unit: Unit) {
        analytic.reportEvent(Analytic.Interaction.CLICK_PREVIOUS_LESSON_IN_STEPS)
        clickLessonBase(direction = Direction.previous,
                unit = unit,
                nextIndex = { index -> index - 1 },
                onOpen = { previousUnit, previousLesson, previousSection -> view?.openPreviousLesson(previousUnit, previousLesson, previousSection) },
                onCantGoAnalytic = { unit -> analytic.reportError(Analytic.Error.ILLEGAL_STATE_PREVIOUS_LESSON, IllegalStateRouteLessonException(unit.id)) },
                onCantGoEvent = { view?.showCantGoPrevious() }
        )
    }

    private fun clickLessonBase(
            direction: Direction,
            unit: Unit,
            nextIndex: (Int) -> Int,
            onOpen: (Unit, Lesson, Section) -> kotlin.Unit,
            onCantGoAnalytic: (Unit) -> kotlin.Unit,
            onCantGoEvent: () -> kotlin.Unit) {
        view?.showLoading()
        threadPoolExecutor.execute {
            val section = sectionRepository.getObject(unit.section)

            var nextUnitId: Long? = null
            val unitIds = section?.units
            val numberOfUnits = unitIds?.size ?: 0
            let {
                unitIds?.forEachIndexed { index, unitId ->
                    if (unit.id == unitId && nextIndex(index) < numberOfUnits && nextIndex(index) >= 0) {
                        nextUnitId = unitIds[nextIndex(index)]
                        return@let  //alias for break
                    }
                }
            }

            nextUnitId?.let {
                val nextUnit = unitRepository.getObject(it)
                if (nextUnit != null && section != null) {
                    val nextLesson = lessonRepository.getObject(nextUnit.lesson)
                    if (nextLesson != null) {
                        mainHandler.post {
                            onOpen.invoke(nextUnit, nextLesson, section)
                        }
                        return@execute
                    }
                }
            }
            if (nextUnitId == null && section != null) {
                //unit in previous or next section
                val course = courseRepository.getObject(section.course)
                val slicedSectionIds = getSlicedSectionIds(direction, section, course)
                slicedSectionIds?.let {
                    val sections = sectionRepository.getObjects(slicedSectionIds)
                    when (direction) {
                        RouteStepPresenter.Direction.previous -> {
                            sections
                                    .reversed()
                                    .forEach {
                                        if (it.hasUserAccessAndNotEmpty(course)) {
                                            it.units?.last()?.let { previousUnitId ->
                                                val previousUnit = unitRepository.getObject(previousUnitId)
                                                if (previousUnit != null) {
                                                    val previousLesson = lessonRepository.getObject(previousUnit.lesson)
                                                    if (previousLesson != null) {
                                                        mainHandler.post {
                                                            onOpen(previousUnit, previousLesson, it)
                                                        }
                                                        return@execute
                                                    }
                                                }
                                            }
                                            return@let
                                        }
                                    }
                        }
                        RouteStepPresenter.Direction.next -> {
                            sections
                                    .forEach { nextSection ->
                                        if (nextSection.hasUserAccessAndNotEmpty(course)) {
                                            nextSection.units?.first()?.let { nextUnitId ->
                                                val nextUnit = unitRepository.getObject(nextUnitId)
                                                nextUnit?.lesson?.let { lessonId ->
                                                    val nextLesson = lessonRepository.getObject(lessonId)
                                                    if (nextLesson != null) {
                                                        mainHandler.post {
                                                            onOpen(nextUnit, nextLesson, nextSection)
                                                        }
                                                        return@execute
                                                    }
                                                }

                                            }
                                            mainHandler.post {
                                            }

                                            return@let
                                        }
                                    }
                        }
                    }
                }
            }

            //when Internet is not available AND when course structure is changing in real time
            //if something is null -> show error
            onCantGoAnalytic.invoke(unit)
            mainHandler.post {
                onCantGoEvent.invoke()
            }
        }
    }

    inner class IllegalStateRouteLessonException(unitId: Long) : IllegalStateException("Next or previous lesson is shouldn't be shown, lessonId = $unitId")

    enum class Direction {
        previous, next
    }
}