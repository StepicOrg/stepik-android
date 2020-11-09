package org.stepik.android.presentation.step_quiz_review

import org.stepik.android.presentation.base.ReduxPresenter
import ru.nobird.android.presentation.redux.container.ReduxViewContainer

class StepQuizReviewPresenter(
    reduxViewContainer: ReduxViewContainer<StepQuizReviewFeature.State, StepQuizReviewFeature.Message, StepQuizReviewFeature.Action.ViewAction>
) : ReduxPresenter<StepQuizReviewFeature.State, StepQuizReviewFeature.Message, StepQuizReviewFeature.Action.ViewAction>(reduxViewContainer)