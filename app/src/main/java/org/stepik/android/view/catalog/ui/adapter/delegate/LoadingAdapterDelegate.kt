package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepik.android.view.catalog_block.model.CatalogItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class LoadingAdapterDelegate : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is CatalogItem.Loading

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> {
        val topMargin = parent.resources.getDimensionPixelSize(R.dimen.loading_placeholder_top_margin)
        val itemView = createView(parent, R.layout.item_course_loading_skeleton_container)
        (itemView.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0, topMargin, 0, 0)
        return LoadingViewHolder(itemView)
    }

    private class LoadingViewHolder(root: View) : DelegateViewHolder<CatalogItem>(root)
}