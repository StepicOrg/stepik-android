package org.stepik.android.view.course_list.ui.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.item_course.view.*
import kotlinx.android.synthetic.main.layout_course_properties.view.*
import org.stepic.droid.R
import org.stepic.droid.util.TextUtil
import org.stepik.android.domain.course.model.CourseStats
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.model.Course
import ru.nobird.app.core.model.safeCast
import java.util.Locale

class CoursePropertiesDelegate(
    root: View,
    private val view: ViewGroup
) {
    private val learnersCountImage = view.learnersCountImage
    private val learnersCountText = view.learnersCountText

    private val courseRatingImage = view.courseRatingImage
    private val courseRatingText = view.courseRatingText

    private val courseCertificateImage = view.courseCertificateImage
    private val courseCertificateText = view.courseCertificateText

    private val courseArchiveImage = view.courseArchiveImage
    private val courseArchiveText = view.courseArchiveText

    private val courseFavoriteImage = root.courseListFavorite
    private val courseWishlistImage = root.courseListWishlist

    fun setStats(courseListItem: CourseListItem.Data) {
        setLearnersCount(courseListItem.course.learnersCount, courseListItem.course.enrollment > 0L)
        setRating(courseListItem.courseStats)
        setCertificate(courseListItem.course)
        setUserCourse(courseListItem.courseStats.enrollmentState.safeCast<EnrollmentState.Enrolled>()?.userCourse)
        setWishlist(courseListItem.course.enrollment > 0L, courseListItem.course.isInWishlist)

        view.isVisible = view.children.any(View::isVisible)
    }

    private fun setLearnersCount(learnersCount: Long, isEnrolled: Boolean) {
        val needShowLearners = learnersCount > 0 && !isEnrolled
        if (needShowLearners) {
            learnersCountText.text = TextUtil.formatNumbers(learnersCount)
        }
        learnersCountImage.isVisible = needShowLearners
        learnersCountText.isVisible = needShowLearners
    }

    private fun setRating(courseStats: CourseStats) {
        val needShow = courseStats.review > 0
        if (needShow) {
            courseRatingText.text = String.format(Locale.ROOT, view.resources.getString(R.string.course_rating_value), courseStats.review)
        }
        courseRatingImage.isVisible = needShow
        courseRatingText.isVisible = needShow
    }

    private fun setCertificate(course: Course) {
        val isEnrolled = course.enrollment > 0L
        val needShow = course.withCertificate && !isEnrolled
        courseCertificateImage.isVisible = needShow
        courseCertificateText.isVisible = needShow
    }

    private fun setUserCourse(userCourse: UserCourse?) {
        courseFavoriteImage.isVisible = userCourse?.isFavorite == true

        val isArchived = userCourse?.isArchived == true
        courseArchiveImage.isVisible = isArchived
        courseArchiveText.isVisible = isArchived
    }

    private fun setWishlist(isEnrolled: Boolean, isWishlisted: Boolean) {
        courseWishlistImage.isVisible = !isEnrolled && isWishlisted
    }
}