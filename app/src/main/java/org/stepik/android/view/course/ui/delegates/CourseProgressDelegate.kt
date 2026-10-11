package org.stepik.android.view.course.ui.delegates

import androidx.core.view.isVisible
import org.stepic.droid.R
import org.stepic.droid.databinding.LayoutCourseProgressBinding
import org.stepic.droid.util.toFixed
import org.stepik.android.model.Progress

class CourseProgressDelegate(
    binding: LayoutCourseProgressBinding,
    onSubmissionCountClicked: () -> Unit,
    private val isLocalSubmissionsEnabled: Boolean
) {
    private val context = binding.root.context

    private val courseProgressCircle = binding.courseProgressCircle
    private val courseProgressValue = binding.courseProgressValue

    private val courseSolutionsTitle = binding.courseSolutionsTitle
    private val courseSolutionsValue = binding.courseSolutionsValue

    init {
        courseSolutionsValue.setOnClickListener { onSubmissionCountClicked() }
    }

    fun setProgress(progress: Progress) {
        val isNeedShowProgress = progress.cost > 0
        courseProgressCircle.isVisible = isNeedShowProgress
        courseProgressValue.isVisible = isNeedShowProgress

        if (isNeedShowProgress) {
            val score = progress
                .score
                ?.toFloatOrNull()
                ?: 0f

            val cost = progress.cost

            courseProgressCircle.progress = (score * 100 / cost) / 100f
            courseProgressValue.text =
                context.getString(R.string.course_content_text_progress_points, score.toFixed(context.resources.getInteger(R.integer.score_decimal_count)), cost)
        }
    }

    fun setSolutionsCount(count: Int) {
        val isNeedShowSolutions = count > 0 && isLocalSubmissionsEnabled
        courseSolutionsTitle.isVisible = isNeedShowSolutions
        courseSolutionsValue.isVisible = isNeedShowSolutions
        courseSolutionsValue.text = count.toString()
    }
}
