package org.stepik.android.presentation.step.mapper

import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step.model.StepNavigationDirection
import org.stepik.android.model.Course
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.view.course_content.model.RequiredSection
import org.stepik.android.view.step.model.SectionUnavailableAction
import org.stepik.android.view.step.model.StepNavigationAction
import java.util.Date
import javax.inject.Inject

class NavigationActionMapper
@Inject
constructor() {
    fun mapToCoursePurchaseAction(course: Course?): StepNavigationAction =
        if (course == null) {
            StepNavigationAction.Unknown
        } else {
            StepNavigationAction.ShowLessonDemoComplete(course)
        }

    fun mapToShowLessonAction(
        direction: StepNavigationDirection,
        lessonData: LessonData,
        isAutoplayEnabled: Boolean = false
    ): StepNavigationAction =
        StepNavigationAction.ShowLesson(direction, lessonData, isAutoplayEnabled)

    fun mapToRequiredSectionAction(
        currentSection: Section?,
        targetSection: Section,
        requiredSection: RequiredSection?
    ): StepNavigationAction =
        if (currentSection == null || requiredSection == null) {
            StepNavigationAction.Unknown
        } else {
            StepNavigationAction.ShowSectionUnavailable(
                SectionUnavailableAction.RequiresSection(
                    currentSection,
                    targetSection,
                    requiredSection
                )
            )
        }

    fun mapToRequiresExamAction(
        currentSection: Section?,
        targetSection: Section,
        requiredSection: RequiredSection?
    ): StepNavigationAction =
        if (currentSection == null) {
            StepNavigationAction.Unknown
        } else {
            StepNavigationAction.ShowSectionUnavailable(
                SectionUnavailableAction.RequiresExam(
                    currentSection,
                    targetSection,
                    requiredSection
                )
            )
        }

    fun mapToRequiresDateAction(
        currentSection: Section?,
        nextLesson: Lesson,
        date: Date
    ): StepNavigationAction =
        if (currentSection == null) {
            StepNavigationAction.Unknown
        } else {
            StepNavigationAction.ShowSectionUnavailable(
                SectionUnavailableAction.RequiresDate(
                    currentSection,
                    nextLesson, date
                )
            )
        }
}