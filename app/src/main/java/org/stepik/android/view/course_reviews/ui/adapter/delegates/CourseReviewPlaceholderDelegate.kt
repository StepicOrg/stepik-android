package org.stepik.android.view.course_reviews.ui.adapter.delegates

import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepic.droid.ui.custom.adapter_delegates.AdapterDelegate
import org.stepic.droid.ui.custom.adapter_delegates.DelegateAdapter
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepik.android.domain.course_reviews.model.CourseReviewItem

class CourseReviewPlaceholderDelegate(
    adapter: DelegateAdapter<CourseReviewItem, DelegateViewHolder<CourseReviewItem>>
) : AdapterDelegate<CourseReviewItem, DelegateViewHolder<CourseReviewItem>>(adapter) {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseReviewItem> =
        ViewHolder(createView(parent, R.layout.view_course_content_unit_placeholder))

    override fun isForViewType(position: Int): Boolean =
        getItemAtPosition(position) is CourseReviewItem.Placeholder

    class ViewHolder(root: View) : DelegateViewHolder<CourseReviewItem>(root)
}