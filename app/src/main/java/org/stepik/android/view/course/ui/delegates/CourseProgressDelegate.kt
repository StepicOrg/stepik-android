package org.stepik.android.view.course.ui.delegates

import android.view.View
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.layout_course_progress.view.*
import org.stepic.droid.R
import org.stepic.droid.util.toFixed
import org.stepik.android.model.Progress

class CourseProgressDelegate(
    view: View,
    onSubmissionCountClicked: () -> Unit,
    private val isLocalSubmissionsEnabled: Boolean
) {
    private val context = view.context

    private val courseProgressCircle = view.courseProgressCircle
    private val courseProgressValue = view.courseProgressValue

    private val courseSolutionsTitle = view.courseSolutionsTitle
    private val courseSolutionsValue = view.courseSolutionsValue

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

            courseProgressCircle.progress = (score.toLong() * 100 / cost) / 100f
            courseProgressValue.text =
                context.getString(R.string.course_content_text_progress_points, score.toFixed(2), cost)
        }
    }

    fun setSolutionsCount(count: Int) {
        val isNeedShowSolutions = count > 0 && isLocalSubmissionsEnabled
        courseSolutionsTitle.isVisible = isNeedShowSolutions
        courseSolutionsValue.isVisible = isNeedShowSolutions
        courseSolutionsValue.text = count.toString()
    }
}