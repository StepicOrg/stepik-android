package org.stepik.android.presentation.course

import org.stepik.android.domain.course.model.CourseHeaderData
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course
import org.stepik.android.presentation.billing.BillingView
import org.stepik.android.presentation.course.model.EnrollmentError

interface CourseView : BillingView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object NetworkError : State()
        object EmptyCourse : State()

        class CourseLoaded(val courseHeaderData: CourseHeaderData) : State()
        class BlockingLoading(val courseHeaderData: CourseHeaderData) : State()
    }

    fun setState(state: State)

    fun showEmptyAuthDialog(course: Course)
    fun showEnrollmentError(errorType: EnrollmentError)
    fun showContinueLearningError()

    fun continueCourse(lastStep: LastStep)
    fun continueAdaptiveCourse(course: Course)

    fun shareCourse(course: Course)
    fun showCourseShareTooltip()

    fun openCoursePurchaseInWeb(courseId: Long)
}
