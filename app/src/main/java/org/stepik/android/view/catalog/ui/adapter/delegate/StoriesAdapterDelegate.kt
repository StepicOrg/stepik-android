package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.view_stories_container.view.*
import org.stepic.droid.R
import org.stepic.droid.features.stories.presentation.StoriesPresenter
import org.stepic.droid.features.stories.presentation.StoriesView
import org.stepic.droid.features.stories.ui.adapter.StoriesAdapter
import org.stepic.droid.ui.util.setHeight
import org.stepik.android.presentation.base.PresenterViewHolder
import org.stepik.android.presentation.catalog.CatalogItem
import ru.nobird.android.stories.model.Story
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class StoriesAdapterDelegate(
    private val onStoryClicked: (Story, Int) -> Unit
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is StoriesPresenter

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        StoriesViewHolder(createView(parent, R.layout.view_stories_container), onStoryClicked = onStoryClicked) as DelegateViewHolder<CatalogItem>

    private class StoriesViewHolder(root: View, onStoryClicked: (Story, Int) -> Unit) : PresenterViewHolder<StoriesView, StoriesPresenter>(root), StoriesView {
        private val storiesPlaceholder = root.storiesContainerLoadingPlaceholder
        private val storiesRecycler = root.storiesRecycler
        private val storiesAdapter = StoriesAdapter(root.context, onStoryClicked = onStoryClicked)

        override fun setState(state: StoriesView.State) {
            when (state) {
                is StoriesView.State.Idle,
                is StoriesView.State.Empty -> {
                    itemView.setHeight(0)
                    storiesRecycler.isVisible = false
                    storiesPlaceholder.isVisible = false
                }

                is StoriesView.State.Loading -> {
                    itemView.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                    storiesRecycler.isVisible = false
                    storiesPlaceholder.isVisible = true
                }

                is StoriesView.State.Success -> {
                    itemView.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                    storiesAdapter.setData(state.stories, state.viewedStoriesIds)
                    storiesRecycler.isVisible = true
                    storiesRecycler.isVisible = false
                }
            }
        }

        override fun attachView(data: StoriesPresenter) {
            data.attachView(this)
        }

        override fun detachView(data: StoriesPresenter) {
            data.detachView(this)
        }
    }
}