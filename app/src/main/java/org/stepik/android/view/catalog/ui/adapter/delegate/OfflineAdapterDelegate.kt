package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_catalog_no_internet_clickable.view.*
import org.stepic.droid.R
import org.stepik.android.view.catalog_block.model.CatalogItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class OfflineAdapterDelegate(
    private val onRetry: () -> Unit
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is CatalogItem.Offline

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        OfflineViewHolder(createView(parent, R.layout.view_catalog_no_internet_clickable), onRetry = onRetry)

    private class OfflineViewHolder(root: View, private val onRetry: () -> Unit) : DelegateViewHolder<CatalogItem>(root) {

        private val placeholderText = root.noInternetPlaceholder

        override fun onBind(data: CatalogItem) {
            data as CatalogItem.Offline
            itemView.setOnClickListener { onRetry() }
            placeholderText.setPlaceholderText(R.string.internet_problem_catalog)
        }
    }
}