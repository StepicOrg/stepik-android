package org.stepik.android.view.course_list.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.stepic.droid.R
import org.stepic.droid.ui.custom.PlaceholderTextView
import org.stepik.android.domain.course_list.model.CourseListItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseListPlaceHolderTextAdapterDelegate : AdapterDelegate<CourseListItem, DelegateViewHolder<CourseListItem>>() {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseListItem> {
        val view = createView(parent, R.layout.course_collection_header_view)
        val margin = parent.context.resources.getDimensionPixelOffset(R.dimen.course_list_padding)
        // TODO Discuss about view and source of colors
        (view.layoutParams as RecyclerView.LayoutParams).setMargins(
            -margin,
            -margin,
            -margin,
            margin
        )
        view.setBackgroundResource(R.drawable.gradient_background_blue_squared)
        return ViewHolder(view)
    }

    override fun isForViewType(position: Int, data: CourseListItem): Boolean =
        data is CourseListItem.PlaceHolderText

    private class ViewHolder(root: View) : DelegateViewHolder<CourseListItem>(root) {

        private val placeholderTextView = root as PlaceholderTextView

        override fun onBind(data: CourseListItem) {
            data as CourseListItem.PlaceHolderText
            placeholderTextView.setPlaceholderText(data.text)
            placeholderTextView.setTextColor(ContextCompat.getColor(context, R.color.text_color_gradient_blue))
        }
    }
}