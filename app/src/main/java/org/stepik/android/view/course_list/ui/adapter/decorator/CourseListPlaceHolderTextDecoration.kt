package org.stepik.android.view.course_list.ui.adapter.decorator

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.stepic.droid.R
import org.stepik.android.domain.course_list.model.CourseListItem
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class CourseListPlaceHolderTextDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = (view.layoutParams as? RecyclerView.LayoutParams)
            ?.viewLayoutPosition
            ?.takeIf { it == 0 }
            ?: return

        val adapter = (parent.adapter as? DefaultDelegateAdapter<CourseListItem>)
            ?: return

        val item = adapter.items.getOrNull(position)
            ?: return

        if (item is CourseListItem.PlaceHolderText) {
            val margin = parent.context.resources.getDimensionPixelOffset(R.dimen.course_list_padding)
            outRect.set(-margin, -margin, -margin, margin)
        }
    }
}