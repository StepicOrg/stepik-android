package org.stepic.droid.features.course.ui.adapter.course_content

import org.stepic.droid.ui.custom.adapter_delegates.DelegateViewHolder
import org.stepic.droid.ui.custom.adapter_delegates.RecyclerViewDelegateAdapter

class CourseContentAdapter : RecyclerViewDelegateAdapter<CourseContentAdapterItem, DelegateViewHolder<CourseContentAdapterItem>>() {
    init {
        addDelegate(CourseContentControlBarDelegate(this))
    }

    override fun getItemAtPosition(position: Int): CourseContentAdapterItem =
            CourseContentAdapterItem.ControlBar

    override fun getItemCount(): Int = 1
}