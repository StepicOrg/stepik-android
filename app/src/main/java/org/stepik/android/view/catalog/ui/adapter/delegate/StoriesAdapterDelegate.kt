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
import org.stepik.android.presentation.catalog.model.CatalogItem
import org.stepik.android.presentation.stories.StoriesFeature
import org.stepik.android.presentation.stories.StoriesViewModel
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.presentation.redux.container.ReduxView
import ru.nobird.android.stories.model.Story
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import timber.log.Timber

class StoriesAdapterDelegate(
    private val onStoryClicked: (Story, Int) -> Unit
) : AdapterDelegate<CatalogItem, DelegateViewHolder<CatalogItem>>() {
    override fun isForViewType(position: Int, data: CatalogItem): Boolean =
        data is StoriesViewModel

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<CatalogItem> =
        StoriesViewHolder(createView(parent, R.layout.view_stories_container), onStoryClicked = onStoryClicked) as DelegateViewHolder<CatalogItem>

    class StoriesViewHolder(root: View, onStoryClicked: (Story, Int) -> Unit)
        : DelegateViewHolder<ReduxView<StoriesFeature.State, StoriesFeature.Action.ViewAction>>(root),
        ReduxView<StoriesFeature.State, StoriesFeature.Action.ViewAction> {
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

        override fun render(state: StoriesFeature.State) {
            viewStateDelegate.switchState(state)
            Timber.d("State: $state")
            if (state is StoriesFeature.State.Success) {
                storiesAdapter.setData(state.stories, state.viewedStoriesIds)
            }
        }

        override fun onAction(action: StoriesFeature.Action.ViewAction) {}
    }
}