package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.error_no_connection_with_button_small.*
import org.stepic.droid.R
import org.stepik.android.view.catalog_block.model.CatalogItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class OfflineAdapterDelegate(
    private val onRetry: () -> Unit
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is CatalogItem.Offline

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> {
        val view = createView(parent, R.layout.error_no_connection_with_button_small)
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            topMargin = parent.resources.getDimensionPixelSize(R.dimen.offline_item_top_margin)
        }
        return OfflineViewHolder(view, onRetry = onRetry)
    }

    private class OfflineViewHolder(
        override val containerView: View,
        private val onRetry: () -> Unit
    ) : DelegateViewHolder<CatalogItem>(containerView), LayoutContainer {

        init {
            tryAgain.setOnClickListener { onRetry() }
        }

        override fun onBind(data: CatalogItem) {
            data as CatalogItem.Offline
            containerView.isVisible = true
        }
    }
}