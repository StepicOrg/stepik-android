package org.stepik.android.presentation.course_continue_redux

import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource

interface CourseContinueFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
    }

    sealed class Message {
        data class OnContinueCourseClicked(val course: Course, val viewSource: CourseViewSource, val interactionSource: CourseContinueInteractionSource) : Message()
        data class ShowStepsContinue(val course: Course, val viewSource: CourseViewSource, val lastStep: LastStep) : Message()
        data class ShowCourseContinue(val course: Course, val viewSource: CourseViewSource, val isAdaptive: Boolean) : Message()
        data class CourseListItemClick(val courseListItem: CourseListItem.Data) : Message()
    }

    sealed class Action {
        data class ContinueCourse(val course: Course, val viewSource: CourseViewSource, val interactionSource: CourseContinueInteractionSource) : Action()
        sealed class ViewAction : Action() {
            data class ShowSteps(val course: Course, val viewSource: CourseViewSource, val lastStep: LastStep) : ViewAction()
            data class ShowCourse(val course: Course, val viewSource: CourseViewSource, val isAdaptive: Boolean) : ViewAction()
            data class OnCourseListItemClick(val courseListItem: CourseListItem.Data) : ViewAction()
        }
    }
}