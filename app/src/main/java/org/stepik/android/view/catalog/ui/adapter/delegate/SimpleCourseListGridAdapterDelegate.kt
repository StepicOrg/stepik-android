package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_block_simple_course_list_grid.*
import org.stepic.droid.R
import org.stepik.android.domain.catalog.model.CatalogCourseList
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class SimpleCourseListGridAdapterDelegate(
    private val onCourseListClicked: (CatalogCourseList) -> Unit
) : AdapterDelegate<CatalogCourseList, DelegateViewHolder<CatalogCourseList>>() {
    override fun isForViewType(position: Int, data: CatalogCourseList): Boolean =
        position > 0

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogCourseList> =
        ViewHolder(createView(parent, R.layout.item_block_simple_course_list_grid))

    private inner class ViewHolder(
        override val containerView: View
    ) : DelegateViewHolder<CatalogCourseList>(containerView), LayoutContainer {

        init {
            containerView.setOnClickListener { onCourseListClicked(itemData ?: return@setOnClickListener) }
        }

        override fun onBind(data: CatalogCourseList) {
            simpleCourseListGridTitle.text = data.title
            itemView.updateLayoutParams<FlexboxLayoutManager.LayoutParams> { flexGrow = 1f }
        }
    }
}