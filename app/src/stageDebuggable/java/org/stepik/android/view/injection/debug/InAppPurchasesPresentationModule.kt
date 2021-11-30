package org.stepik.android.view.injection.debug

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.debug.InAppPurchasesFeature
import org.stepik.android.presentation.debug.InAppPurchasesViewModel
import org.stepik.android.presentation.debug.dispatcher.InAppPurchasesActionDispatcher
import org.stepik.android.presentation.debug.reducer.InAppPurchasesReducer
import ru.nobird.android.presentation.redux.container.wrapWithViewContainer
import ru.nobird.android.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.android.presentation.redux.feature.ReduxFeature

@Module
object InAppPurchasesPresentationModule {
    /**
     * Presentation
     */
    @Provides
    @IntoMap
    @ViewModelKey(InAppPurchasesViewModel::class)
    internal fun provideInAppPurchasesPresenter(
        inAppPurchasesReducer: InAppPurchasesReducer,
        inAppPurchasesActionDispatcher: InAppPurchasesActionDispatcher
    ): ViewModel =
        InAppPurchasesViewModel(
            ReduxFeature(InAppPurchasesFeature.State.Idle, inAppPurchasesReducer)
                .wrapWithActionDispatcher(inAppPurchasesActionDispatcher)
                .wrapWithViewContainer()
        )
}