package org.stepik.android.view.injection.debug

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.debug.SplitGroupFeature
import org.stepik.android.presentation.debug.SplitGroupViewModel
import org.stepik.android.presentation.debug.dispatcher.SplitGroupActionDispatcher
import org.stepik.android.presentation.debug.reducer.SplitGroupReducer
import ru.nobird.android.presentation.redux.container.wrapWithViewContainer
import ru.nobird.android.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.android.presentation.redux.feature.ReduxFeature

@Module
object SplitGroupPresentationModule {
    /**
     * Presentation
     */
    @Provides
    @IntoMap
    @ViewModelKey(SplitGroupViewModel::class)
    internal fun provideSplitGroupPresenter(
        splitGroupReducer: SplitGroupReducer,
        splitGroupActionDispatcher: SplitGroupActionDispatcher
    ): ViewModel =
        SplitGroupViewModel(
            ReduxFeature(SplitGroupFeature.State.Idle, splitGroupReducer)
                .wrapWithActionDispatcher(splitGroupActionDispatcher)
                .wrapWithViewContainer()
        )
}