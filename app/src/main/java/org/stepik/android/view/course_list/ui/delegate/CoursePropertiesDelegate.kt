package org.stepik.android.view.course_list.ui.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isVisible
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemCourseBinding
import org.stepic.droid.databinding.LayoutCoursePropertiesBinding
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
    private val rootBinding = ItemCourseBinding.bind(root)
    private val propertiesBinding = LayoutCoursePropertiesBinding.bind(view)

    private val learnersCountImage = propertiesBinding.learnersCountImage
    private val learnersCountText = propertiesBinding.learnersCountText

    private val courseRatingImage = propertiesBinding.courseRatingImage
    private val courseRatingText = propertiesBinding.courseRatingText

    private val courseCertificateImage = propertiesBinding.courseCertificateImage
    private val courseCertificateText = propertiesBinding.courseCertificateText

    private val courseArchiveImage = propertiesBinding.courseArchiveImage
    private val courseArchiveText = propertiesBinding.courseArchiveText

    private val courseFavoriteImage = rootBinding.courseListFavorite
    private val courseWishlistImage = rootBinding.courseListWishlist

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
