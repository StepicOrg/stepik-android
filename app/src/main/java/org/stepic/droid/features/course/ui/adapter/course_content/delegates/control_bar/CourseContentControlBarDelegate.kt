package org.stepic.droid.features.course.ui.adapter.course_content.delegates.control_bar

import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepic.droid.features.course.ui.model.course_content.CourseContentItem
import org.stepic.droid.ui.custom.adapter_delegates.AdapterDelegate
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepic.droid.ui.custom.adapter_delegates.DelegateAdapter

class CourseContentControlBarDelegate(
        adapter: DelegateAdapter<CourseContentItem, DelegateViewHolder<CourseContentItem>>
) : AdapterDelegate<CourseContentItem, DelegateViewHolder<CourseContentItem>>(adapter) {

    override fun onCreateViewHolder(parent: ViewGroup) =
            ViewHolder(createView(parent, R.layout.view_course_content_control_bar))

    override fun isForViewType(position: Int): Boolean =
            getItemAtPosition(position) == CourseContentItem.ControlBar

    class ViewHolder(root: View) : DelegateViewHolder<CourseContentItem>(root) {
        override fun onBind(data: CourseContentItem) {

        }
    }
}