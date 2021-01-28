package org.stepik.android.view.injection.story_deeplink

import dagger.Subcomponent
import org.stepik.android.view.story_deeplink.ui.dialog.StoryDeepLinkDialogFragment

@Subcomponent(modules = [
    StoryDeepLinkPresentationModule::class
])
interface StoryDeepLinkComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): StoryDeepLinkComponent
    }

    fun inject(storyDeepLinkDialogFragment: StoryDeepLinkDialogFragment)
}