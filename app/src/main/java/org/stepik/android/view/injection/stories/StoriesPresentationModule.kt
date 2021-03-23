package org.stepik.android.view.injection.stories

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.stories.StoriesFeature
import org.stepik.android.presentation.stories.dispatcher.StoriesActionDispatcher
import org.stepik.android.presentation.stories.reducer.StoriesReducer
import org.stepik.android.view.stories.viewmodel.StoriesViewModel
import ru.nobird.android.presentation.redux.container.wrapWithViewContainer
import ru.nobird.android.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.android.presentation.redux.feature.ReduxFeature

@Module
internal object StoriesPresentationModule {
    @Provides
    @IntoMap
    @ViewModelKey(StoriesViewModel::class)
    internal fun provideStoriesViewModel(
        reducer: StoriesReducer,
        dispatcher: StoriesActionDispatcher
    ): ViewModel =
        StoriesViewModel(
            ReduxFeature(StoriesFeature.State.Idle, reducer)
                .wrapWithActionDispatcher(dispatcher)
                .wrapWithViewContainer()
        )
}