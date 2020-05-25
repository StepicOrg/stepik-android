package org.stepik.android.view.course_reviews.ui.adapter.delegates

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_course_review_summary_item.view.*
import org.stepic.droid.R
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
        private val summaryAverage = root.summaryAverage
        private val summaryRating = root.summaryRating
        private val summaryCount = root.summaryCount

        private val summaryDistribution = listOf(
            root.summaryCount1Progress to root.summaryCount1Value,
            root.summaryCount2Progress to root.summaryCount2Value,
            root.summaryCount3Progress to root.summaryCount3Value,
            root.summaryCount4Progress to root.summaryCount4Value,
            root.summaryCount5Progress to root.summaryCount5Value
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