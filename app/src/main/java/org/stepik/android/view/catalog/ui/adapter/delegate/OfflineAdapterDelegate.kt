package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_catalog_no_internet_clickable.view.*
import org.stepic.droid.R
import org.stepik.android.presentation.catalog.model.CatalogItem
import org.stepik.android.presentation.catalog.model.OfflinePlaceholder
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class OfflineAdapterDelegate(
    private val onRetry: () -> Unit
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is OfflinePlaceholder

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        OfflineViewHolder(createView(parent, R.layout.view_catalog_no_internet_clickable), onRetry = onRetry) as DelegateViewHolder<CatalogItem>

    private inner class OfflineViewHolder(root: View, private val onRetry: () -> Unit) : DelegateViewHolder<OfflinePlaceholder>(root) {

        private val placeholderText = root.noInternetPlaceholder

        override fun onBind(data: OfflinePlaceholder) {
            itemView.setOnClickListener { onRetry() }
            placeholderText.setPlaceholderText(R.string.internet_problem_catalog)
        }
    }
}