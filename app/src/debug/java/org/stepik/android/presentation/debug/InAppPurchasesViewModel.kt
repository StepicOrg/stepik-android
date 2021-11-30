package org.stepik.android.presentation.debug

import ru.nobird.android.presentation.redux.container.ReduxViewContainer
import ru.nobird.android.view.redux.viewmodel.ReduxViewModel

class InAppPurchasesViewModel(
    reduxViewContainer: ReduxViewContainer<InAppPurchasesFeature.State, InAppPurchasesFeature.Message, InAppPurchasesFeature.Action.ViewAction>
) : ReduxViewModel<InAppPurchasesFeature.State, InAppPurchasesFeature.Message, InAppPurchasesFeature.Action.ViewAction>(reduxViewContainer)