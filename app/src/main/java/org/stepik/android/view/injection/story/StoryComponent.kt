package org.stepik.android.view.injection.story

import dagger.Subcomponent
import org.stepic.droid.features.stories.ui.activity.StoriesActivity

@Subcomponent(
    modules = [
        StoryDataModule::class,
        StoryPresentationModule::class
    ]
)
interface StoryComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): StoryComponent
    }

    fun inject(storiesActivity: StoriesActivity)
}