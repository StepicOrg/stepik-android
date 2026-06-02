package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ErrorNoConnectionWithButtonSmallBinding
import org.stepik.android.view.catalog.model.CatalogItem
import ru.nobird.app.core.model.safeCast
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
        }
        return OfflineViewHolder(view, onRetry = onRetry)
    }

    private class OfflineViewHolder(
        root: View,
        private val onRetry: () -> Unit
    ) : DelegateViewHolder<CatalogItem>(root) {
        private val viewBinding: ErrorNoConnectionWithButtonSmallBinding by viewBinding { ErrorNoConnectionWithButtonSmallBinding.bind(root) }

        init {
            viewBinding.tryAgain.setOnClickListener { onRetry() }
            root.isVisible = true
        }

        override fun onBind(data: CatalogItem) {
            data as CatalogItem.Offline

            itemView.doOnLayout {
                val parent = it.parent.safeCast<View>() ?: return@doOnLayout
                val remainingHeight = parent.height - itemView.bottom - itemView.top
                if (remainingHeight > 0) {
                    itemView.updateLayoutParams {
                        height = itemView.height + remainingHeight
                    }
                }
            }
        }
    }
}
