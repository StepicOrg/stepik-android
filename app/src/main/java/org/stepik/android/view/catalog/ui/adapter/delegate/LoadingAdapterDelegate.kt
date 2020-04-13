package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepik.android.presentation.catalog.model.CatalogItem
import org.stepik.android.presentation.catalog.model.LoadingPlaceholder
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class LoadingAdapterDelegate : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is LoadingPlaceholder

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        LoadingViewHolder(createView(parent, R.layout.course_item_loading_view_container)) as DelegateViewHolder<CatalogItem>

    private inner class LoadingViewHolder(root: View) : DelegateViewHolder<LoadingPlaceholder>(root)
}