package org.stepic.droid.features.course.ui.adapter.course_content

import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepic.droid.ui.custom.adapter_delegates.AdapterDelegate
import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepic.droid.ui.custom.adapter_delegates.RecyclerViewDelegateAdapter

class CourseContentControlBarDelegate(
        adapter: RecyclerViewDelegateAdapter<CourseContentAdapterItem, DelegateViewHolder<CourseContentAdapterItem>>
) : AdapterDelegate<CourseContentAdapterItem, DelegateViewHolder<CourseContentAdapterItem>>(adapter) {

    override fun onCreateViewHolder(parent: ViewGroup) =
            ViewHolder(createView(parent, R.layout.view_course_content_control_bar))

    override fun isForViewType(position: Int): Boolean =
            getItemAtPosition(position) == CourseContentAdapterItem.ControlBar

    class ViewHolder(root: View) : DelegateViewHolder<CourseContentAdapterItem>(root) {
        override fun onBind(data: CourseContentAdapterItem) {

        }
    }
}