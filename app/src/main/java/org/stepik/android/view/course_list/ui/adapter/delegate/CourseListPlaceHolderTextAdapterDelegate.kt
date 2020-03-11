package org.stepik.android.view.course_list.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepic.droid.ui.custom.PlaceholderTextView
import org.stepik.android.domain.course_list.model.CourseListItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseListPlaceHolderTextAdapterDelegate : AdapterDelegate<CourseListItem, DelegateViewHolder<CourseListItem>>() {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseListItem> =
        ViewHolder(createView(parent, R.layout.course_collection_header_view))

    override fun isForViewType(position: Int, data: CourseListItem): Boolean =
        data is CourseListItem.PlaceHolderText

    private class ViewHolder(root: View) : DelegateViewHolder<CourseListItem>(root) {

        private val placeholderTextView = root as PlaceholderTextView

        override fun onBind(data: CourseListItem) {
            data as CourseListItem.PlaceHolderText
            placeholderTextView.setPlaceholderText(data.text)
        }
    }
}