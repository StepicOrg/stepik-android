package org.stepik.android.presentation.step_quiz_review

import ru.nobird.android.presentation.redux.container.ReduxViewContainer
import ru.nobird.android.view.redux.viewmodel.ReduxViewModel

class StepQuizReviewViewModel(
    reduxViewContainer: ReduxViewContainer<StepQuizReviewFeature.State, StepQuizReviewFeature.Message, StepQuizReviewFeature.Action.ViewAction>
) : ReduxViewModel<StepQuizReviewFeature.State, StepQuizReviewFeature.Message, StepQuizReviewFeature.Action.ViewAction>(reduxViewContainer)