package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.flexbox.FlexboxLayoutManager
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemBlockSimpleCourseListGridBinding
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

    private inner class ViewHolder(root: android.view.View) : DelegateViewHolder<CatalogCourseList>(root) {
        private val viewBinding: ItemBlockSimpleCourseListGridBinding by viewBinding { ItemBlockSimpleCourseListGridBinding.bind(root) }

        init {
            root.setOnClickListener { onCourseListClicked(itemData ?: return@setOnClickListener) }
        }

        override fun onBind(data: CatalogCourseList) {
            viewBinding.simpleCourseListGridTitle.text = data.title
            itemView.updateLayoutParams<FlexboxLayoutManager.LayoutParams> { flexGrow = 1f }
        }
    }
}
