package org.stepik.android.view.course_search.adapter.delegate

import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepik.android.domain.course_search.model.CourseSearchResultListItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseSearchResultLoadingAdapterDelegate : AdapterDelegate<CourseSearchResultListItem, DelegateViewHolder<CourseSearchResultListItem>>() {
    override fun isForViewType(position: Int, data: CourseSearchResultListItem): Boolean =
        data is CourseSearchResultListItem.Placeholder

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseSearchResultListItem> =
        ViewHolder(createView(parent, R.layout.item_course_search_result_loading))

    private class ViewHolder(root: View) : DelegateViewHolder<CourseSearchResultListItem>(root)
}