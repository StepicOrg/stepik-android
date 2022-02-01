package org.stepik.android.presentation.debug

import ru.nobird.android.view.redux.viewmodel.ReduxViewModel
import ru.nobird.app.presentation.redux.container.ReduxViewContainer

class InAppPurchasesViewModel(
    reduxViewContainer: ReduxViewContainer<InAppPurchasesFeature.State, InAppPurchasesFeature.Message, InAppPurchasesFeature.Action.ViewAction>
) : ReduxViewModel<InAppPurchasesFeature.State, InAppPurchasesFeature.Message, InAppPurchasesFeature.Action.ViewAction>(reduxViewContainer)