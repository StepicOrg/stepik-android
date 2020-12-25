package org.stepik.android.view.injection.story

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.story.StoryActionDispatcher
import org.stepik.android.presentation.story.StoryFeature
import org.stepik.android.presentation.story.StoryReducer
import org.stepik.android.view.story.viewmodel.StoryViewModel
import ru.nobird.android.presentation.redux.container.wrapWithViewContainer
import ru.nobird.android.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.android.presentation.redux.feature.ReduxFeature

@Module
internal object StoryPresentationModule {
    @Provides
    @IntoMap
    @ViewModelKey(StoryViewModel::class)
    internal fun provideStoryViewModel(
        reducer: StoryReducer,
        dispatcher: StoryActionDispatcher
    ): ViewModel =
        StoryViewModel(
            ReduxFeature(StoryFeature.State.Idle, reducer)
                .wrapWithActionDispatcher(dispatcher)
                .wrapWithViewContainer()
        )
}