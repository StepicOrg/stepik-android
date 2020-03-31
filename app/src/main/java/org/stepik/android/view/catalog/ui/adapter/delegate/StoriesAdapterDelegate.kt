package org.stepik.android.view.catalog.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.view_stories_container.view.*
import org.stepic.droid.R
import org.stepic.droid.features.stories.presentation.StoriesView
import org.stepic.droid.features.stories.ui.adapter.StoriesAdapter
import org.stepic.droid.ui.util.setHeight
import org.stepik.android.presentation.base.PresenterViewHolder
import ru.nobird.android.presentation.base.PresenterBase
import ru.nobird.android.stories.model.Story
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class StoriesAdapterDelegate(
    private val onStoryClicked: (Story, Int) -> Unit
) : AdapterDelegate<PresenterBase<StoriesView>, DelegateViewHolder<PresenterBase<StoriesView>>>() {
    override fun isForViewType(position: Int, data: PresenterBase<StoriesView>): Boolean =
        data is StoriesView

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<PresenterBase<StoriesView>> =
        StoriesViewHolder(createView(parent, R.layout.view_stories_container), onStoryClicked = onStoryClicked)

    private class StoriesViewHolder(root: View, onStoryClicked: (Story, Int) -> Unit) : PresenterViewHolder<StoriesView>(root), StoriesView {
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

        override fun attachView(data: PresenterBase<StoriesView>) {
            data.attachView(this)
        }

        override fun detachView(data: PresenterBase<StoriesView>) {
            data.detachView(this)
        }
    }
}