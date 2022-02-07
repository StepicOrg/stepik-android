package org.stepic.droid.features.stories.ui.activity

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.features.stories.ui.delegate.StoriesActivityDelegate
import org.stepik.android.presentation.story.StoryFeature
import org.stepik.android.view.story.viewmodel.StoryViewModel
import ru.nobird.app.presentation.redux.container.ReduxView
import ru.nobird.android.stories.ui.custom.DismissableLayout
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import javax.inject.Inject

class StoriesActivity : FragmentActivityBase(), ReduxView<StoryFeature.State, StoryFeature.Action.ViewAction> {
    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val storyViewModel: StoryViewModel by reduxViewModel(this) { viewModelFactory }

    private lateinit var storiesDelegate: StoriesActivityDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = if (Build.VERSION.SDK_INT == 26) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        injectComponent()
        storyViewModel.onNewMessage(StoryFeature.Message.Init)

        setContentView(R.layout.activity_stories)
        storiesDelegate = StoriesActivityDelegate(this, analytic) { storyId, storyPosition, storyReaction ->
            storyViewModel.onNewMessage(StoryFeature.Message.OnReactionClicked(storyId, storyPosition, storyReaction))
        }
        storiesDelegate.onCreate(savedInstanceState)

        storiesDelegate.dismissableLayout.addDismissListener(object : DismissableLayout.DismissListener {
            override fun onDismiss() {
                val story = storiesDelegate.getCurrentStory()
                if (story != null) {
                    analytic.reportAmplitudeEvent(AmplitudeAnalytic.Stories.STORY_CLOSED, mapOf(
                        AmplitudeAnalytic.Stories.Values.STORY_ID to story.id,
                        AmplitudeAnalytic.Stories.Values.CLOSE_TYPE to AmplitudeAnalytic.Stories.Values.CloseTypes.SWIPE
                    ))
                }
            }
        })
    }

    private fun injectComponent() {
        App.component()
            .storyComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onResume() {
        super.onResume()
        storiesDelegate.onResume()
    }

    override fun onPause() {
        storiesDelegate.onPause()
        super.onPause()
    }

    override fun onBackPressed() {
        storiesDelegate.finish()

        val story = storiesDelegate.getCurrentStory() ?: return

        analytic.reportAmplitudeEvent(AmplitudeAnalytic.Stories.STORY_CLOSED, mapOf(
            AmplitudeAnalytic.Stories.Values.STORY_ID to story.id,
            AmplitudeAnalytic.Stories.Values.CLOSE_TYPE to AmplitudeAnalytic.Stories.Values.CloseTypes.CROSS
        ))
    }

    override fun onAction(action: StoryFeature.Action.ViewAction) {
        // no op
    }

    override fun render(state: StoryFeature.State) {
        val votes =
            when (state) {
                is StoryFeature.State.Content ->
                    state.reactions

                else ->
                    emptyMap()
            }
        storiesDelegate.setStoryVotes(votes)
    }
}