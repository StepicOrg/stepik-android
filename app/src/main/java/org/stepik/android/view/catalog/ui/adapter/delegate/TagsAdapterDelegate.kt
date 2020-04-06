package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.view_catalog_tags.view.*
import org.stepic.droid.R
import org.stepic.droid.model.StepikFilter
import org.stepic.droid.ui.adapters.TagsAdapter
import org.stepik.android.model.Tag
import org.stepik.android.presentation.base.PresenterViewHolder
import org.stepik.android.presentation.catalog.CatalogItem
import org.stepik.android.presentation.catalog.FiltersPresenter
import org.stepik.android.presentation.catalog.FiltersView
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import java.util.EnumSet

class TagsAdapterDelegate(
    private val onTagClicked: (Tag) -> Unit
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is FiltersPresenter

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        TagsViewHolder(createView(parent, R.layout.view_catalog_tags), onTagClicked = onTagClicked) as DelegateViewHolder<CatalogItem>

    private class TagsViewHolder(root: View, onTagClicked: (Tag) -> Unit) : PresenterViewHolder<FiltersView, FiltersPresenter>(root), FiltersView {

        private val tagsRecyclerView = root.tagsRecycler
        private val tagsAdapter = TagsAdapter(onTagClicked)

        init {
            tagsRecyclerView.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            tagsRecyclerView.adapter = tagsAdapter
        }

        override fun onFiltersPrepared(filters: EnumSet<StepikFilter>) {
            TODO("Not yet implemented")
        }

        override fun attachView(data: FiltersPresenter) {
            data.attachView(this)
        }

        override fun detachView(data: FiltersPresenter) {
            data.detachView(this)
        }
    }
}