package org.stepik.android.view.injection.debug

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.debug.DebugFeature
import org.stepik.android.presentation.debug.DebugViewModel
import org.stepik.android.presentation.debug.dispatcher.DebugActionDispatcher
import org.stepik.android.presentation.debug.reducer.DebugReducer
import ru.nobird.android.presentation.redux.container.wrapWithViewContainer
import ru.nobird.android.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.android.presentation.redux.feature.ReduxFeature

@Module
object DebugPresentationModule {
    /**
     * Presentation
     */
    @Provides
    @IntoMap
    @ViewModelKey(DebugViewModel::class)
    internal fun provideDebugPresenter(
        debugReducer: DebugReducer,
        debugActionDispatcher: DebugActionDispatcher
    ): ViewModel =
        DebugViewModel(
            ReduxFeature(DebugFeature.State.Idle, debugReducer)
                .wrapWithActionDispatcher(debugActionDispatcher)
                .wrapWithViewContainer()
        )
}