package org.stepic.droid.features.stories.ui.delegate

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_stories.*
import ru.nobird.android.stories.ui.custom.DismissableLayout
import ru.nobird.android.stories.ui.delegate.StoriesActivityDelegateBase
import ru.nobird.android.stories.ui.delegate.StoryPartViewDelegate

class StoriesActivityDelegate(
        activity: AppCompatActivity
) : StoriesActivityDelegateBase(activity) {
    override val dismissableLayout: DismissableLayout =
            activity.content

    override val storiesViewPager: ViewPager =
            activity.storiesPager

    override val arguments: Bundle =
            activity.intent.extras ?: Bundle.EMPTY

    override val storyPartDelegates: List<StoryPartViewDelegate> =
            listOf(PlainTextWithButtonStoryPartDelegate(activity))
}