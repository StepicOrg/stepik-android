package org.stepik.android.presentation.course

import org.stepik.android.domain.course.model.CourseHeaderData
import org.stepik.android.model.Course
import org.stepik.android.presentation.course.model.EnrollmentError
import org.stepik.android.presentation.course_continue.CourseContinueView
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData
import org.stepik.android.presentation.user_courses.model.UserCourseAction
import org.stepik.android.presentation.wishlist.model.WishlistAction

interface CourseView : CourseContinueView {
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

    fun showSaveUserCourseSuccess(userCourseAction: UserCourseAction)
    fun showSaveUserCourseError(userCourseAction: UserCourseAction)
    fun showWishlistActionSuccess(wishlistAction: WishlistAction)
    fun showWishlistActionFailure(wishlistAction: WishlistAction)

    fun shareCourse(course: Course)
    fun showCourseShareTooltip()

    fun openCoursePurchaseInWeb(courseId: Long, queryParams: Map<String, List<String>>? = null)
    fun openCoursePurchaseInApp(coursePurchaseData: CoursePurchaseData)

    fun showTrialLesson(lessonId: Long)
}
