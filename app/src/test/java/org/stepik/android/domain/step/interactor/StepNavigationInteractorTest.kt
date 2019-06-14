package org.stepik.android.domain.step.interactor

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.lesson.repository.LessonRepository
import org.stepik.android.domain.section.repository.SectionRepository
import org.stepik.android.domain.step.model.StepNavigationDirection
import org.stepik.android.domain.unit.repository.UnitRepository
import org.stepik.android.model.Course
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Step
import org.stepik.android.model.Unit
import java.util.EnumSet

@RunWith(MockitoJUnitRunner::class)
class StepNavigationInteractorTest {

    @Mock
    private lateinit var sectionRepository: SectionRepository

    @Mock
    private lateinit var unitRepository: UnitRepository

    @Mock
    private lateinit var lessonRepository: LessonRepository

    @Test
    fun stepNavigationDirections_onlyStep() {
        val stepNavigationInteractor = StepNavigationInteractor(sectionRepository, unitRepository, lessonRepository)

        val step = Step(id = 100, position = 2)
        val lesson = Lesson(id = 200, steps = longArrayOf(0, step.id, 2))
        val unit = Unit(id = 300, lesson = lesson.id)
        val section = Section(id = 400, units = listOf(unit.id))
        val course = Course(id = 500, sections = longArrayOf(section.id))

        val lessonData = LessonData(lesson, unit, section, course)

        verifyNoMoreInteractions(sectionRepository)
        verifyNoMoreInteractions(unitRepository)
        verifyNoMoreInteractions(lessonRepository)

        stepNavigationInteractor
            .getStepNavigationDirections(step, lessonData)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertResult(EnumSet.noneOf(StepNavigationDirection::class.java))
    }

    @Test
    fun stepNavigationDirections_middleUnit_singleStep() {
        val stepNavigationInteractor = StepNavigationInteractor(sectionRepository, unitRepository, lessonRepository)

        val step = Step(id = 100, position = 1)
        val lesson = Lesson(id = 200, steps = longArrayOf(step.id))

        val prevUnit = Unit(id = 299, lesson = 0, position = 1)
        val unit = Unit(id = 300, lesson = lesson.id, position = 2)
        val nextUnit = Unit(id = 301, lesson = 0, position = 3)

        val section = Section(id = 400, units = listOf(prevUnit.id, unit.id, nextUnit.id), position = 1)
        val course = Course(id = 500, sections = longArrayOf(section.id))

        val lessonData = LessonData(lesson, unit, section, course)

        verifyNoMoreInteractions(sectionRepository)
        verifyNoMoreInteractions(unitRepository)
        verifyNoMoreInteractions(lessonRepository)

        stepNavigationInteractor
            .getStepNavigationDirections(step, lessonData)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertResult(EnumSet.allOf(StepNavigationDirection::class.java))
    }

    @Test
    fun stepNavigationDirections_middleSection_singleStep() {
        val step = Step(id = 100, position = 1)
        val lesson = Lesson(id = 200, steps = longArrayOf(step.id))

        val unit = Unit(id = 300, lesson = lesson.id, position = 1)

        val prevSection = Section(id = 399, units = listOf(0), position = 1, isActive = true, isRequirementSatisfied = true, isExam = false)
        val section = Section(id = 400, units = listOf(unit.id), position = 2)
        val nextSection = Section(id = 401, units = listOf(0), position = 3, isActive = true, isRequirementSatisfied = true, isExam = false)
        val course = Course(id = 500, sections = longArrayOf(prevSection.id, section.id, nextSection.id), enrollment = 500)

        val lessonData = LessonData(lesson, unit, section, course)

        whenever(sectionRepository.getSections(prevSection.id)) doReturn Single.just(listOf(prevSection))
        whenever(sectionRepository.getSections(nextSection.id)) doReturn Single.just(listOf(nextSection))

        verifyNoMoreInteractions(sectionRepository)
        verifyNoMoreInteractions(unitRepository)
        verifyNoMoreInteractions(lessonRepository)

        val stepNavigationInteractor = StepNavigationInteractor(sectionRepository, unitRepository, lessonRepository)

        stepNavigationInteractor
            .getStepNavigationDirections(step, lessonData)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertResult(EnumSet.allOf(StepNavigationDirection::class.java))
    }

    @Test
    fun stepNavigationDirections_middleSection_firstNotLastStep() {
        val step = Step(id = 100, position = 1)
        val nextStep = Step(id = 101, position = 2)
        val lesson = Lesson(id = 200, steps = longArrayOf(step.id, nextStep.id))

        val unit = Unit(id = 300, lesson = lesson.id, position = 1)

        val prevSection = Section(id = 399, units = listOf(0), position = 1, isActive = true, isRequirementSatisfied = true, isExam = false)
        val section = Section(id = 400, units = listOf(unit.id), position = 2)
        val nextSection = Section(id = 401, units = listOf(0), position = 3, isActive = true, isRequirementSatisfied = true, isExam = false)
        val course = Course(id = 500, sections = longArrayOf(prevSection.id, section.id, nextSection.id), enrollment = 500)

        val lessonData = LessonData(lesson, unit, section, course)

        whenever(sectionRepository.getSections(prevSection.id)) doReturn Single.just(listOf(prevSection))
        whenever(sectionRepository.getSections(nextSection.id)) doReturn Single.just(listOf(nextSection))

        verifyNoMoreInteractions(sectionRepository)
        verifyNoMoreInteractions(unitRepository)
        verifyNoMoreInteractions(lessonRepository)

        val stepNavigationInteractor = StepNavigationInteractor(sectionRepository, unitRepository, lessonRepository)

        stepNavigationInteractor
            .getStepNavigationDirections(step, lessonData)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertResult(EnumSet.of(StepNavigationDirection.PREV))
    }

    @Test
    fun stepNavigationDirections_middleSection_LastNotFirstStep() {
        val prevStep = Step(id = 99, position = 1)
        val step = Step(id = 100, position = 2)
        val lesson = Lesson(id = 200, steps = longArrayOf(prevStep.id, step.id))

        val unit = Unit(id = 300, lesson = lesson.id, position = 1)

        val prevSection = Section(id = 399, units = listOf(0), position = 1, isActive = true, isRequirementSatisfied = true, isExam = false)
        val section = Section(id = 400, units = listOf(unit.id), position = 2)
        val nextSection = Section(id = 401, units = listOf(0), position = 3, isActive = true, isRequirementSatisfied = true, isExam = false)
        val course = Course(id = 500, sections = longArrayOf(prevSection.id, section.id, nextSection.id), enrollment = 500)

        val lessonData = LessonData(lesson, unit, section, course)

        whenever(sectionRepository.getSections(prevSection.id)) doReturn Single.just(listOf(prevSection))
        whenever(sectionRepository.getSections(nextSection.id)) doReturn Single.just(listOf(nextSection))

        verifyNoMoreInteractions(sectionRepository)
        verifyNoMoreInteractions(unitRepository)
        verifyNoMoreInteractions(lessonRepository)

        val stepNavigationInteractor = StepNavigationInteractor(sectionRepository, unitRepository, lessonRepository)

        stepNavigationInteractor
            .getStepNavigationDirections(step, lessonData)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertResult(EnumSet.of(StepNavigationDirection.NEXT))
    }

    @Test
    fun stepNavigationDirections_middleSection_emptyPrevSection_singleStep() {
        val step = Step(id = 100, position = 1)
        val lesson = Lesson(id = 200, steps = longArrayOf(step.id))

        val unit = Unit(id = 300, lesson = lesson.id, position = 1)

        val prevSection = Section(id = 399, units = emptyList(), position = 1, isActive = true, isRequirementSatisfied = true, isExam = false)
        val section = Section(id = 400, units = listOf(unit.id), position = 2)
        val nextSection = Section(id = 401, units = listOf(0), position = 3, isActive = true, isRequirementSatisfied = true, isExam = false)
        val course = Course(id = 500, sections = longArrayOf(prevSection.id, section.id, nextSection.id), enrollment = 500)

        val lessonData = LessonData(lesson, unit, section, course)

        whenever(sectionRepository.getSections(prevSection.id)) doReturn Single.just(listOf(prevSection))
        whenever(sectionRepository.getSections(nextSection.id)) doReturn Single.just(listOf(nextSection))

        verifyNoMoreInteractions(sectionRepository)
        verifyNoMoreInteractions(unitRepository)
        verifyNoMoreInteractions(lessonRepository)

        val stepNavigationInteractor = StepNavigationInteractor(sectionRepository, unitRepository, lessonRepository)

        stepNavigationInteractor
            .getStepNavigationDirections(step, lessonData)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertResult(EnumSet.of(StepNavigationDirection.NEXT))
    }

    @Test
    fun stepNavigationDirections_middleSection_emptyNextSection_singleStep() {
        val step = Step(id = 100, position = 1)
        val lesson = Lesson(id = 200, steps = longArrayOf(step.id))

        val unit = Unit(id = 300, lesson = lesson.id, position = 1)

        val prevSection = Section(id = 399, units = listOf(0), position = 1, isActive = true, isRequirementSatisfied = true, isExam = false)
        val section = Section(id = 400, units = listOf(unit.id), position = 2)
        val nextSection = Section(id = 401, units = emptyList(), position = 3, isActive = true, isRequirementSatisfied = true, isExam = false)
        val course = Course(id = 500, sections = longArrayOf(prevSection.id, section.id, nextSection.id), enrollment = 500)

        val lessonData = LessonData(lesson, unit, section, course)

        whenever(sectionRepository.getSections(prevSection.id)) doReturn Single.just(listOf(prevSection))
        whenever(sectionRepository.getSections(nextSection.id)) doReturn Single.just(listOf(nextSection))

        verifyNoMoreInteractions(sectionRepository)
        verifyNoMoreInteractions(unitRepository)
        verifyNoMoreInteractions(lessonRepository)

        val stepNavigationInteractor = StepNavigationInteractor(sectionRepository, unitRepository, lessonRepository)

        stepNavigationInteractor
            .getStepNavigationDirections(step, lessonData)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertResult(EnumSet.of(StepNavigationDirection.PREV))
    }
}