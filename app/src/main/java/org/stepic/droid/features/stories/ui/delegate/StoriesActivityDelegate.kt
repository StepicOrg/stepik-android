package org.stepic.droid.features.stories.ui.delegate

import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_stories.*
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import ru.nobird.android.stories.model.Story
import ru.nobird.android.stories.ui.adapter.StoriesPagerAdapter
import ru.nobird.android.stories.ui.custom.DismissableLayout
import ru.nobird.android.stories.ui.delegate.StoriesActivityDelegateBase
import ru.nobird.android.stories.ui.delegate.StoryPartViewDelegate

class StoriesActivityDelegate(
        activity: AppCompatActivity,
        private val analytic: Analytic
) : StoriesActivityDelegateBase(activity) {
    public override val dismissableLayout: DismissableLayout =
            activity.content

    public override val storiesViewPager: ViewPager =
            activity.storiesPager

    override val arguments: Bundle =
            activity.intent.extras ?: Bundle.EMPTY

    override val storyPartDelegates: List<StoryPartViewDelegate> =
            listOf(PlainTextWithButtonStoryPartDelegate(analytic, activity))

    override fun onComplete() {
        super.onComplete()

        val story = getCurrentStory() ?: return

        analytic.reportAmplitudeEvent(AmplitudeAnalytic.Stories.STORY_CLOSED, mapOf(
                AmplitudeAnalytic.Stories.Values.STORY_ID to story.id,
                AmplitudeAnalytic.Stories.Values.CLOSE_TYPE to AmplitudeAnalytic.Stories.Values.CloseTypes.AUTO
        ))
    }

    fun getCurrentStory(): Story? =
            (storiesViewPager.adapter as? StoriesPagerAdapter)
                    ?.stories
                    ?.getOrNull(storiesViewPager.currentItem)
}