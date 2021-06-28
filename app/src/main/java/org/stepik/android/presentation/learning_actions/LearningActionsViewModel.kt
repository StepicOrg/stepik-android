package org.stepik.android.presentation.learning_actions

import ru.nobird.android.presentation.redux.container.ReduxViewContainer
import ru.nobird.android.view.redux.viewmodel.ReduxViewModel

class LearningActionsViewModel(
    reduxViewContainer: ReduxViewContainer<LearningActionsFeature.State, LearningActionsFeature.Message, LearningActionsFeature.Action.ViewAction>
) : ReduxViewModel<LearningActionsFeature.State, LearningActionsFeature.Message, LearningActionsFeature.Action.ViewAction>(reduxViewContainer)