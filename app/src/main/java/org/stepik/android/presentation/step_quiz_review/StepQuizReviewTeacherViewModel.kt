package org.stepik.android.presentation.step_quiz_review

import ru.nobird.app.presentation.redux.container.ReduxViewContainer
import ru.nobird.android.view.redux.viewmodel.ReduxViewModel

class StepQuizReviewTeacherViewModel(
    reduxViewContainer: ReduxViewContainer<StepQuizReviewTeacherFeature.State, StepQuizReviewTeacherFeature.Message, StepQuizReviewTeacherFeature.Action.ViewAction>
) : ReduxViewModel<StepQuizReviewTeacherFeature.State, StepQuizReviewTeacherFeature.Message, StepQuizReviewTeacherFeature.Action.ViewAction>(reduxViewContainer)