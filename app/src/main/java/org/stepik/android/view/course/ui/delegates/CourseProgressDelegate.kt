package org.stepik.android.view.course.ui.delegates

import android.view.View
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.layout_course_progress.view.*
import org.stepic.droid.R
import org.stepik.android.model.Progress

class CourseProgressDelegate(
    view: View
) {
    private val context = view.context

    private val courseProgressCircle = view.courseProgressCircle
    private val courseProgressValue = view.courseProgressValue

    fun setProgress(progress: Progress) {
        val isNeedShowProgress = progress.cost > 0
        courseProgressCircle.isVisible = isNeedShowProgress
        courseProgressValue.isVisible = isNeedShowProgress

        if (isNeedShowProgress) {
            val score = progress
                .score
                ?.toFloatOrNull()
                ?.toLong()
                ?: 0L

            val cost = progress.cost

            courseProgressCircle.progress = (score * 100 / cost) / 100f
            courseProgressValue.text =
                context.getString(R.string.course_content_text_progress_points, score, cost)
        }
    }
}