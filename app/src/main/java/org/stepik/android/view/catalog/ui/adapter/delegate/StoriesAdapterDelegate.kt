package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ViewStoriesContainerBinding
import org.stepic.droid.features.stories.ui.adapter.StoriesAdapter
import org.stepik.android.presentation.stories.StoriesFeature
import org.stepik.android.view.catalog.model.CatalogItem
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.stories.model.Story
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class StoriesAdapterDelegate(
    private val onStoryClicked: (Story, Int) -> Unit
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is CatalogItem.Stories

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        StoriesViewHolder(createView(parent, R.layout.view_stories_container), onStoryClicked = onStoryClicked)

    class StoriesViewHolder(root: android.view.View, onStoryClicked: (Story, Int) -> Unit) : DelegateViewHolder<CatalogItem>(root) {
        private val viewBinding: ViewStoriesContainerBinding by viewBinding { ViewStoriesContainerBinding.bind(itemView) }
        private val storiesPlaceholder = viewBinding.storiesContainerLoadingPlaceholder
        val storiesRecycler = viewBinding.storiesRecycler
        val storiesAdapter = StoriesAdapter(onStoryClicked = onStoryClicked)

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

        override fun onBind(data: CatalogItem) {
            data as CatalogItem.Stories
            render(data.state)
        }

        private fun render(state: StoriesFeature.State) {
            viewStateDelegate.switchState(state)
            if (state is StoriesFeature.State.Success) {
                storiesAdapter.setData(state.stories, state.viewedStoriesIds)
                storiesAdapter.selected = -1
            }
        }
    }
}