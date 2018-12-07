package org.stepik.android.view.course_content.ui.adapter.delegates.control_bar

import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepik.android.view.course_content.model.CourseContentItem
import org.stepic.droid.ui.custom.adapter_delegates.AdapterDelegate
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepic.droid.ui.custom.adapter_delegates.DelegateAdapter

class CourseContentControlBarDelegate(
        adapter: DelegateAdapter<CourseContentItem, DelegateViewHolder<CourseContentItem>>
) : AdapterDelegate<CourseContentItem, DelegateViewHolder<CourseContentItem>>(adapter) {

    override fun onCreateViewHolder(parent: ViewGroup) =
        ViewHolder(
            createView(parent, R.layout.view_course_content_control_bar)
        )

    override fun isForViewType(position: Int): Boolean =
            getItemAtPosition(position) == CourseContentItem.ControlBar

    class ViewHolder(root: View) : DelegateViewHolder<CourseContentItem>(root) {
        override fun onBind(data: CourseContentItem) {

        }
    }
}