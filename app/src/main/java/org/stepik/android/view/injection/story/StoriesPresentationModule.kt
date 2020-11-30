package org.stepik.android.view.injection.story

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.stories.StoriesFeature
import org.stepik.android.presentation.stories.StoriesViewModel
import org.stepik.android.presentation.stories.dispatcher.StoriesActionDispatcher
import org.stepik.android.presentation.stories.reducer.StoriesReducer
import ru.nobird.android.presentation.redux.container.wrapWithViewContainer
import ru.nobird.android.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.android.presentation.redux.feature.ReduxFeature

@Module
object StoriesPresentationModule {

    @Provides
    @IntoMap
    @ViewModelKey(StoriesViewModel::class)
    internal fun provideStoriesPresenter(
        storiesReducer: StoriesReducer,
        storiesActionDispatcher: StoriesActionDispatcher
    ): ViewModel =
        StoriesViewModel(
            ReduxFeature(StoriesFeature.State.Idle, storiesReducer)
                .wrapWithActionDispatcher(storiesActionDispatcher)
                .wrapWithViewContainer()
        )
}