package org.stepik.android.presentation.step_quiz

import org.stepik.android.presentation.base.ReduxPresenter
import ru.nobird.android.presentation.redux.container.ReduxViewContainer

class StepQuizPresenter(
    reduxViewContainer: ReduxViewContainer<StepQuizFeature.State, StepQuizFeature.Message, StepQuizFeature.Action.ViewAction>
) : ReduxPresenter<StepQuizFeature.State, StepQuizFeature.Message, StepQuizFeature.Action.ViewAction>(reduxViewContainer)