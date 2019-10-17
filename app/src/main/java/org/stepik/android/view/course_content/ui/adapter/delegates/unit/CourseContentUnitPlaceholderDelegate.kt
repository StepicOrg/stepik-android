package org.stepik.android.view.course_content.ui.adapter.delegates.unit

import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepik.android.view.course_content.model.CourseContentItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseContentUnitPlaceholderDelegate : AdapterDelegate<CourseContentItem, DelegateViewHolder<CourseContentItem>>() {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseContentItem> =
        ViewHolder(createView(parent, R.layout.view_course_content_unit_placeholder))

    override fun isForViewType(position: Int, data: CourseContentItem): Boolean =
        data is CourseContentItem.UnitItemPlaceholder

    private class ViewHolder(root: View) : DelegateViewHolder<CourseContentItem>(root)
}