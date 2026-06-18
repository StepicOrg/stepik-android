package org.stepik.android.view.course.ui.delegates

import androidx.core.view.isVisible
import org.stepic.droid.databinding.LayoutCourseStatsBinding
import org.stepik.android.domain.course.model.CourseStats
import kotlin.math.roundToInt

class CourseStatsDelegate(
    binding: LayoutCourseStatsBinding
) {
    companion object {
        private const val MIN_FEATURED_READINESS = 0.9
    }

    private val courseRating = binding.courseRating
    private val courseLearnersCount = binding.courseLearnersCount
    private val courseFeatured = binding.courseFeatured

    fun setStats(courseStats: CourseStats) {
        courseRating.total = 5
        courseRating.progress = courseStats.review.roundToInt()
        courseRating.isVisible = courseStats.review > 0

        courseLearnersCount.text = courseStats.learnersCount.toString()
        courseFeatured.isVisible = courseStats.readiness > MIN_FEATURED_READINESS
    }
}
