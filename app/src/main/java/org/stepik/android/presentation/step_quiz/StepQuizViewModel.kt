package org.stepik.android.presentation.step_quiz

import ru.nobird.android.presentation.redux.container.ReduxViewContainer
import ru.nobird.android.view.redux.viewmodel.ReduxViewModel

class StepQuizViewModel(
    reduxViewContainer: ReduxViewContainer<StepQuizFeature.State, StepQuizFeature.Message, StepQuizFeature.Action.ViewAction>
) : ReduxViewModel<StepQuizFeature.State, StepQuizFeature.Message, StepQuizFeature.Action.ViewAction>(reduxViewContainer)