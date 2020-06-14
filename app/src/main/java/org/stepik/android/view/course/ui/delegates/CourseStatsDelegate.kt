package org.stepik.android.view.course.ui.delegates

import android.view.View
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.layout_course_stats.view.*
import org.stepik.android.domain.course.model.CourseStats
import kotlin.math.roundToInt

class CourseStatsDelegate(
    view: View
) {
    companion object {
        private const val MIN_FEATURED_READINESS = 0.9
    }

    private val courseRating = view.courseRating
    private val courseLearnersCount = view.courseLearnersCount
    private val courseFeatured = view.courseFeatured

    fun setStats(courseStats: CourseStats) {
        courseRating.total = 5
        courseRating.progress = courseStats.review.roundToInt()
        courseRating.isVisible = courseStats.review > 0

        courseLearnersCount.text = courseStats.learnersCount.toString()
        courseFeatured.isVisible = courseStats.readiness > MIN_FEATURED_READINESS
    }
}