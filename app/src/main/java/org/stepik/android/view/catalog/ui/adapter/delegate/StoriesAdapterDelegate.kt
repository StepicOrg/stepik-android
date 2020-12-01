package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.view_stories_container.view.*
import org.stepic.droid.R
import org.stepic.droid.features.stories.ui.adapter.StoriesAdapter
import org.stepik.android.presentation.stories.StoriesFeature
import org.stepik.android.view.catalog_block.model.CatalogBlockItem
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.stories.model.Story
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import timber.log.Timber

class StoriesAdapterDelegate(
    private val onStoryClicked: (Story, Int) -> Unit
) : AdapterDelegate<CatalogBlockItem, DelegateViewHolder<CatalogBlockItem>>() {
    override fun isForViewType(position: Int, data: CatalogBlockItem): Boolean =
        data is CatalogBlockItem.StoriesBlock

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogBlockItem> =
        StoriesViewHolder(createView(parent, R.layout.view_stories_container), onStoryClicked = onStoryClicked)

    class StoriesViewHolder(root: View, onStoryClicked: (Story, Int) -> Unit) : DelegateViewHolder<CatalogBlockItem>(root) {
        private val storiesPlaceholder = root.storiesContainerLoadingPlaceholder
        val storiesRecycler = root.storiesRecycler
        val storiesAdapter = StoriesAdapter(root.context, onStoryClicked = onStoryClicked)

        private val viewStateDelegate = ViewStateDelegate<StoriesFeature.State>()

        init {
            storiesRecycler.itemAnimator = null
            storiesRecycler.adapter = storiesAdapter
            storiesRecycler.layoutManager = LinearLayoutManager(root.context, LinearLayoutManager.HORIZONTAL, false)

            viewStateDelegate.addState<StoriesFeature.State.Idle>()
            viewStateDelegate.addState<StoriesFeature.State.Empty>()
            viewStateDelegate.addState<StoriesFeature.State.Loading>(storiesPlaceholder)
            viewStateDelegate.addState<StoriesFeature.State.Success>(storiesRecycler)
        }

        override fun onBind(data: CatalogBlockItem) {
            data as CatalogBlockItem.StoriesBlock
            render(data.state)
        }

        private fun render(state: StoriesFeature.State) {
            viewStateDelegate.switchState(state)
            Timber.d("State: $state")
            if (state is StoriesFeature.State.Success) {
                storiesAdapter.setData(state.stories, state.viewedStoriesIds)
            }
        }
    }
}