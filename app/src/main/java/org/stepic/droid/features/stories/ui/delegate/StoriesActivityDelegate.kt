package org.stepic.droid.features.stories.ui.delegate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_stories.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepik.android.domain.story.model.StoryReaction
import ru.nobird.android.core.model.safeCast
import ru.nobird.android.stories.model.Story
import ru.nobird.android.stories.ui.adapter.StoriesPagerAdapter
import ru.nobird.android.stories.ui.custom.DismissableLayout
import ru.nobird.android.stories.ui.custom.StoryView
import ru.nobird.android.stories.ui.delegate.StoriesActivityDelegateBase
import ru.nobird.android.stories.ui.delegate.StoryPartViewDelegate

class StoriesActivityDelegate(
    activity: AppCompatActivity,
    private val analytic: Analytic,
    storyReactionListener: (storyId: Long, storyPosition: Int, storyReaction: StoryReaction) -> Unit
) : StoriesActivityDelegateBase(activity) {
    private val storyReactions = mutableMapOf<Long, StoryReaction>()
    private val storyPartDelegate =
        PlainTextWithButtonStoryPartDelegate(analytic, activity, storyReactions, storyReactionListener)

    public override val dismissableLayout: DismissableLayout =
        activity.content

    public override val storiesViewPager: ViewPager =
        activity.storiesPager

    override val arguments: Bundle =
        activity.intent.extras ?: Bundle.EMPTY

    override val storyPartDelegates: List<StoryPartViewDelegate> =
        listOf(storyPartDelegate)

    override fun onComplete() {
        super.onComplete()

        val story = getCurrentStory() ?: return

        analytic.reportAmplitudeEvent(AmplitudeAnalytic.Stories.STORY_CLOSED, mapOf(
            AmplitudeAnalytic.Stories.Values.STORY_ID to story.id,
            AmplitudeAnalytic.Stories.Values.CLOSE_TYPE to AmplitudeAnalytic.Stories.Values.CloseTypes.AUTO
        ))
    }

    fun getCurrentStory(): Story? =
        storiesViewPager.adapter
            .safeCast<StoriesPagerAdapter>()
            ?.stories
            ?.getOrNull(storiesViewPager.currentItem)

    fun setStoryVotes(votes: Map<Long, StoryReaction>) {
        val diff = votes - storyReactions // only this way as reactions can't be removed

        storyReactions.clear()
        storyReactions += votes

        val adapter = storiesViewPager.adapter
            .safeCast<StoriesPagerAdapter>() ?: return

        diff.forEach { (storyId, _) ->
            val position = adapter.stories
                .indexOfFirst { it.id == storyId }

            val story = adapter.stories
                .getOrNull(position)

            val storyPartPager = storiesViewPager
                .findViewWithTag<StoryView>(position)
                ?.findViewById<ViewPager>(R.id.storyViewPager)

            storyPartPager?.children?.forEach { view ->
                storyPartDelegate.setUpReactions(story, view, position)
            }
        }
    }
}