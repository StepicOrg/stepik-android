package org.stepik.android.view.injection.debug

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.debug.SplitTestsFeature
import org.stepik.android.presentation.debug.SplitTestsViewModel
import org.stepik.android.presentation.debug.dispatcher.SplitTestsActionDispatcher
import org.stepik.android.presentation.debug.reducer.SplitTestsReducer
import ru.nobird.app.presentation.redux.container.wrapWithViewContainer
import ru.nobird.app.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.app.presentation.redux.feature.ReduxFeature

@Module
object SplitTestsPresentationModule {
    /**
     * Presentation
     */
    @Provides
    @IntoMap
    @ViewModelKey(SplitTestsViewModel::class)
    internal fun provideSplitTestsPresenter(
        splitTestsReducer: SplitTestsReducer,
        splitTestsActionDispatcher: SplitTestsActionDispatcher
    ): ViewModel =
        SplitTestsViewModel(
            ReduxFeature(SplitTestsFeature.State.Idle, splitTestsReducer)
                .wrapWithActionDispatcher(splitTestsActionDispatcher)
                .wrapWithViewContainer()
        )
}