package org.stepik.android.presentation.debug

import ru.nobird.android.presentation.redux.container.ReduxViewContainer
import ru.nobird.android.view.redux.viewmodel.ReduxViewModel

class SplitGroupViewModel(
    reduxViewContainer: ReduxViewContainer<SplitGroupFeature.State, SplitGroupFeature.Message, SplitGroupFeature.Action.ViewAction>
) : ReduxViewModel<SplitGroupFeature.State, SplitGroupFeature.Message, SplitGroupFeature.Action.ViewAction>(reduxViewContainer)