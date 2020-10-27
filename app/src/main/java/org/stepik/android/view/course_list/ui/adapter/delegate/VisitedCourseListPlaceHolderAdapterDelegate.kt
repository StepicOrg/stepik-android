package org.stepik.android.view.course_list.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepik.android.domain.course_list.model.CourseListItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class VisitedCourseListPlaceHolderAdapterDelegate : AdapterDelegate<CourseListItem, DelegateViewHolder<CourseListItem>>() {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseListItem> =
        ViewHolder(createView(parent, R.layout.item_visited_course_skeleton))

    override fun isForViewType(position: Int, data: CourseListItem): Boolean =
        data is CourseListItem.PlaceHolder

    private class ViewHolder(root: View) : DelegateViewHolder<CourseListItem>(root)
}