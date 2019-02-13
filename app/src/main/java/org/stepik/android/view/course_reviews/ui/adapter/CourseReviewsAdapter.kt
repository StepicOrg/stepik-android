package org.stepik.android.view.course_reviews.ui.adapter

import android.support.v7.util.DiffUtil
import org.stepic.droid.ui.custom.adapter_delegates.DelegateAdapter
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepik.android.domain.course_reviews.model.CourseReviewItem
import org.stepik.android.view.course_reviews.ui.adapter.delegates.CourseReviewDelegate

class CourseReviewsAdapter: DelegateAdapter<CourseReviewItem, DelegateViewHolder<CourseReviewItem>>() {
    var items: List<CourseReviewItem> = emptyList()
        set(value) {
            DiffUtil
                .calculateDiff(CourseReviewsDiffCallback(field, value))
                .dispatchUpdatesTo(this)
            field = value
        }

    init {
        addDelegate(CourseReviewDelegate(this))
    }

    override fun getItemAtPosition(position: Int): CourseReviewItem =
        items[position]

    override fun getItemCount(): Int =
        items.size
}