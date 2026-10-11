package org.stepik.android.view.course_reviews.ui.adapter.delegates

import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepic.droid.databinding.ViewCourseReviewSummaryItemBinding
import org.stepic.droid.util.safeDiv
import org.stepik.android.domain.course_reviews.model.CourseReviewItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import kotlin.math.roundToInt

class CourseReviewSummaryDelegate : AdapterDelegate<CourseReviewItem, DelegateViewHolder<CourseReviewItem>>() {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseReviewItem> =
        ViewHolder(createView(parent, R.layout.view_course_review_summary_item))

    override fun isForViewType(position: Int, data: CourseReviewItem): Boolean =
        data is CourseReviewItem.Summary

    private class ViewHolder(root: View) : DelegateViewHolder<CourseReviewItem>(root) {
        private val viewBinding = ViewCourseReviewSummaryItemBinding.bind(root)

        private val summaryAverage = viewBinding.summaryAverage
        private val summaryRating = viewBinding.summaryRating
        private val summaryCount = viewBinding.summaryCount

        private val summaryDistribution = listOf(
            viewBinding.summaryCount1Progress to viewBinding.summaryCount1Value,
            viewBinding.summaryCount2Progress to viewBinding.summaryCount2Value,
            viewBinding.summaryCount3Progress to viewBinding.summaryCount3Value,
            viewBinding.summaryCount4Progress to viewBinding.summaryCount4Value,
            viewBinding.summaryCount5Progress to viewBinding.summaryCount5Value
        )

        init {
            summaryRating.total = 5
        }

        override fun onBind(data: CourseReviewItem) {
            data as CourseReviewItem.Summary

            with(data.courseReviewSummary) {
                summaryAverage.text = context.getString(R.string.course_rating_value, average)
                summaryRating.progress = average.roundToInt()
                summaryCount.text = count.toString()

                distribution.forEachIndexed { index: Int, l: Long ->
                    val (progress, value) = summaryDistribution[index]
                    progress.progress = (l * 100 safeDiv count).toInt()
                    value.text = l.toString()
                }
            }
        }
    }
}
