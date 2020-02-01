package org.stepik.android.view.course_reviews.ui.adapter.delegates

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_course_review_summary_item.view.*
import org.stepic.droid.R
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

        init {
            summaryRating.total = 5
        }

        override fun onBind(data: CourseReviewItem) {
            data as CourseReviewItem.Summary

            with(data.courseReviewSummary) {
                summaryAverage.text = context.getString(R.string.course_rating_value, average)
                summaryRating.progress = average.roundToInt()
                summaryCount.text = count.toString()
            }
        }
    }
}