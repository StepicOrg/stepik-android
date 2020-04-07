package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.view_stories_container.view.*
import org.stepic.droid.R
import org.stepic.droid.features.stories.presentation.StoriesPresenter
import org.stepic.droid.features.stories.presentation.StoriesView
import org.stepic.droid.features.stories.ui.adapter.StoriesAdapter
import org.stepik.android.presentation.base.PresenterViewHolder
import org.stepik.android.presentation.catalog.CatalogItem
import org.stepik.android.view.ui.delegate.ViewStateDelegate
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

    class StoriesViewHolder(root: View, onStoryClicked: (Story, Int) -> Unit) : PresenterViewHolder<StoriesView, StoriesPresenter>(root), StoriesView {
        private val storiesPlaceholder = root.storiesContainerLoadingPlaceholder
        val storiesRecycler = root.storiesRecycler
        val storiesAdapter = StoriesAdapter(root.context, onStoryClicked = onStoryClicked)

        private val viewStateDelegate = ViewStateDelegate<StoriesView.State>()

        init {
            storiesRecycler.itemAnimator = null
            storiesRecycler.adapter = storiesAdapter
            storiesRecycler.layoutManager = LinearLayoutManager(root.context, LinearLayoutManager.HORIZONTAL, false)

            viewStateDelegate.addState<StoriesView.State.Idle>()
            viewStateDelegate.addState<StoriesView.State.Empty>()
            viewStateDelegate.addState<StoriesView.State.Loading>(storiesPlaceholder)
            viewStateDelegate.addState<StoriesView.State.Success>(storiesRecycler)
        }

        override fun setState(state: StoriesView.State) {
            viewStateDelegate.switchState(state)
            if (state is StoriesView.State.Success) {
                storiesAdapter.setData(state.stories, state.viewedStoriesIds)
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