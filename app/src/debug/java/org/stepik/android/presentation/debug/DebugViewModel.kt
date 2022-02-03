package org.stepik.android.presentation.debug

import ru.nobird.app.presentation.redux.container.ReduxViewContainer
import ru.nobird.android.view.redux.viewmodel.ReduxViewModel

class DebugViewModel(
    reduxViewContainer: ReduxViewContainer<DebugFeature.State, DebugFeature.Message, DebugFeature.Action.ViewAction>
) : ReduxViewModel<DebugFeature.State, DebugFeature.Message, DebugFeature.Action.ViewAction>(reduxViewContainer)