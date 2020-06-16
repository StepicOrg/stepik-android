package org.stepik.android.view.course_list.ui.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.layout_course_properties.view.*
import org.stepic.droid.R
import org.stepic.droid.util.safeDiv
import org.stepic.droid.util.toFixed
import org.stepik.android.domain.course.model.CourseStats
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.model.Course
import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

class CoursePropertiesDelegate(
    private val view: ViewGroup
) {
    private val learnersCountImage = view.learnersCountImage
    private val learnersCountText = view.learnersCountText

    private val courseItemProgress = view.courseItemProgressView
    private val courseItemProgressTitle = view.courseItemProgressTitle

    private val courseRatingImage = view.courseRatingImage
    private val courseRatingText = view.courseRatingText

    private val courseCertificateImage = view.courseCertificateImage
    private val courseCertificateText = view.courseCertificateText

    fun setStats(courseListItem: CourseListItem.Data) {
        setLearnersCount(courseListItem.course.learnersCount, courseListItem.course.enrollment > 0L)
        setProgress(courseListItem.courseStats)
        setRating(courseListItem.courseStats)
        setCertificate(courseListItem.course)

        view.isVisible = view.children.any(View::isVisible)
    }

    private fun setLearnersCount(learnersCount: Long, isEnrolled: Boolean) {
        val needShowLearners = learnersCount > 0 && !isEnrolled
        if (needShowLearners) {
            learnersCountText.text = formatLearners(learnersCount)
        }
        learnersCountImage.isVisible = needShowLearners
        learnersCountText.isVisible = needShowLearners
    }

    private fun formatLearners(learnersCount: Long): String {
        if (learnersCount < 10000) return learnersCount.toString()
        val exp = (ln(learnersCount.toDouble()) / ln(1000.0)).toInt()
        val decimalFormat = DecimalFormat("0.#")
        return "${decimalFormat.format(learnersCount / 1000.0.pow(exp.toDouble()))}${"KMGTPE"[exp - 1]}"
    }

    private fun setProgress(courseStats: CourseStats) {
        val progress = courseStats.progress
        val needShow = if (progress != null && progress.cost > 0) {
            val score = progress
                .score
                ?.toFloatOrNull()
                ?: 0f

            prepareViewForProgress(score, progress.cost)
            true
        } else {
            false
        }
        courseItemProgress.isVisible = needShow
        courseItemProgressTitle.isVisible = needShow
    }

    private fun prepareViewForProgress(score: Float, cost: Long) {
        courseItemProgress.progress = (score * 100 safeDiv cost) / 100f
        courseItemProgressTitle.text = view
            .resources
            .getString(R.string.course_content_text_progress, score.toFixed(view.resources.getInteger(R.integer.score_decimal_count)), cost)
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
        val needShow = course.hasCertificate && !isEnrolled
        courseCertificateImage.isVisible = needShow
        courseCertificateText.isVisible = needShow
    }
}