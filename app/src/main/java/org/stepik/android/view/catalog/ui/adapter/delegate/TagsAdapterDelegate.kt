package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.view_catalog_tags.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.adapters.TagsAdapter
import org.stepic.droid.ui.util.setHeight
import org.stepik.android.model.Tag
import org.stepik.android.presentation.base.PresenterViewHolder
import org.stepik.android.presentation.catalog.CatalogItem
import org.stepik.android.presentation.catalog.TagsPresenter
import org.stepik.android.presentation.catalog.TagsView
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class TagsAdapterDelegate(
    private val onTagClicked: (Tag) -> Unit
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is TagsPresenter

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        TagsViewHolder(createView(parent, R.layout.view_catalog_tags), onTagClicked = onTagClicked) as DelegateViewHolder<CatalogItem>

    private class TagsViewHolder(root: View, onTagClicked: (Tag) -> Unit) : PresenterViewHolder<TagsView, TagsPresenter>(root), TagsView {

        private val tagsRecyclerView = root.tagsRecycler
        private val tagsAdapter = TagsAdapter(onTagClicked)

        private val viewStateDelegate =  ViewStateDelegate<TagsView.State>()

        init {
            tagsRecyclerView.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            tagsRecyclerView.adapter = tagsAdapter

            viewStateDelegate.addState<TagsView.State.Idle>()
            viewStateDelegate.addState<TagsView.State.Empty>()
            viewStateDelegate.addState<TagsView.State.Loading>()
            viewStateDelegate.addState<TagsView.State.TagsLoaded>(itemView, tagsRecyclerView)
        }

        override fun setState(state: TagsView.State) {
            viewStateDelegate.switchState(state)
            when (state) {
                is TagsView.State.Idle,
                is TagsView.State.Empty,
                is TagsView.State.Loading -> {
                    itemView.setHeight(0)
                }
                is TagsView.State.TagsLoaded -> {
                    itemView.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                    tagsAdapter.setTags(state.tags)
                }
            }
        }

        override fun attachView(data: TagsPresenter) {
            data.attachView(this)
        }

        override fun detachView(data: TagsPresenter) {
            data.detachView(this)
        }
    }
}