package org.stepik.android.view.stories.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.view_stories_container.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.features.stories.ui.activity.StoriesActivity
import org.stepic.droid.features.stories.ui.adapter.StoriesAdapter
import org.stepik.android.presentation.stories.StoriesFeature
import org.stepik.android.view.stories.viewmodel.StoriesViewModel
import ru.nobird.android.presentation.redux.container.ReduxView
import ru.nobird.android.stories.transition.SharedTransitionIntentBuilder
import ru.nobird.android.stories.transition.SharedTransitionsManager
import ru.nobird.android.stories.ui.delegate.SharedTransitionContainerDelegate
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import javax.inject.Inject

class StoriesFragment : Fragment(R.layout.view_stories_container), ReduxView<StoriesFeature.State, StoriesFeature.Action.ViewAction> {

    companion object {
        const val TAG = "StoriesFragment"

        fun newInstance(): Fragment =
            StoriesFragment()

        private const val HOME_STORIES_KEY = "home_stories"
    }

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewStateDelegate: ViewStateDelegate<StoriesFeature.State>

    private lateinit var storiesAdapter: StoriesAdapter

    private val storiesViewModel: StoriesViewModel by reduxViewModel(this) { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponent()
        storiesViewModel.onNewMessage(StoriesFeature.Message.InitMessage())
    }

    private fun injectComponent() {
        App.component()
            .storiesComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<StoriesFeature.State.Idle>()
        viewStateDelegate.addState<StoriesFeature.State.Empty>()
        viewStateDelegate.addState<StoriesFeature.State.Loading>(storiesContainerLoadingPlaceholder)
        viewStateDelegate.addState<StoriesFeature.State.Success>(storiesRecycler)

        storiesAdapter = StoriesAdapter { _, position ->
            requireContext().startActivity(
                SharedTransitionIntentBuilder.createIntent(
                    requireContext(), StoriesActivity::class.java,
                    HOME_STORIES_KEY, position, storiesAdapter.stories)
            )
        }

        with(storiesRecycler) {
            itemAnimator = null
            adapter = storiesAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    override fun onStart() {
        super.onStart()
        SharedTransitionsManager.registerTransitionDelegate(HOME_STORIES_KEY, object :
            SharedTransitionContainerDelegate {
            override fun getSharedView(position: Int): View? {
                val storyViewHolder = storiesRecycler.findViewHolderForAdapterPosition(position) as? StoriesAdapter.StoryViewHolder
                    ?: return null

                return storyViewHolder.cover
            }

            override fun onPositionChanged(position: Int) {
                storiesRecycler.layoutManager?.scrollToPosition(position)
                storiesAdapter.selected = position

                if (position != -1) {
                    val story = storiesAdapter.stories[position]
                    storiesViewModel.onNewMessage(StoriesFeature.Message.StoryViewed(story.id))
                    analytic.reportAmplitudeEvent(
                        AmplitudeAnalytic.Stories.STORY_OPENED, mapOf(
                        AmplitudeAnalytic.Stories.Values.STORY_ID to story.id,
                        AmplitudeAnalytic.Stories.Values.SOURCE to AmplitudeAnalytic.Stories.Values.Source.HOME
                    ))
                }
            }
        })
    }

    override fun onStop() {
        SharedTransitionsManager.unregisterTransitionDelegate(HOME_STORIES_KEY)
        super.onStop()
    }

    override fun onAction(action: StoriesFeature.Action.ViewAction) {}

    override fun render(state: StoriesFeature.State) {
        viewStateDelegate.switchState(state)
        if (state is StoriesFeature.State.Success) {
            storiesAdapter.setData(state.stories, state.viewedStoriesIds)
        }
    }
}