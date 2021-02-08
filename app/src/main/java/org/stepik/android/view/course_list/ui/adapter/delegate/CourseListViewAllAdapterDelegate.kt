package org.stepik.android.view.course_list.ui.adapter.delegate

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.TextViewCompat
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_course_view_all.*
import org.stepic.droid.R
import org.stepic.droid.util.resolveColorAttribute
import org.stepik.android.domain.course_list.model.CourseListItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class CourseListViewAllAdapterDelegate(
    private val onViewClick: () -> Unit
) : AdapterDelegate<CourseListItem, DelegateViewHolder<CourseListItem>>() {
    override fun isForViewType(position: Int, data: CourseListItem): Boolean =
        data is CourseListItem.ViewAll

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CourseListItem> =
        ViewHolder(createView(parent, R.layout.item_course_view_all))

    private inner class ViewHolder(
        override val containerView: View
    ) : DelegateViewHolder<CourseListItem>(containerView), LayoutContainer {

        init {
            containerView.setOnClickListener { onViewClick() }
        }

        override fun onBind(data: CourseListItem) {
            TextViewCompat.setCompoundDrawableTintList(viewAllText, ColorStateList.valueOf(viewAllText.context.resolveColorAttribute(R.attr.colorPrimary)))
        }
    }
}