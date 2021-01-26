package org.stepik.android.view.injection.story_deeplink

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.story_deeplink.StoryDeepLinkPresenter

@Module
abstract class StoryDeepLinkPresentationModule {
    /**
     * Presentation
     */
    @Binds
    @IntoMap
    @ViewModelKey(StoryDeepLinkPresenter::class)
    internal abstract fun bindStoryDeepLinkPresenter(storyDeepLinkPresenter: StoryDeepLinkPresenter): ViewModel
}