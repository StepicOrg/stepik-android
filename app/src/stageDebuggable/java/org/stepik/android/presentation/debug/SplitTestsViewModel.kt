package org.stepik.android.presentation.debug

import ru.nobird.android.presentation.redux.container.ReduxViewContainer
import ru.nobird.android.view.redux.viewmodel.ReduxViewModel

class SplitTestsViewModel(
    reduxViewContainer: ReduxViewContainer<SplitTestsFeature.State, SplitTestsFeature.Message, SplitTestsFeature.Action.ViewAction>
) : ReduxViewModel<SplitTestsFeature.State, SplitTestsFeature.Message, SplitTestsFeature.Action.ViewAction>(reduxViewContainer)