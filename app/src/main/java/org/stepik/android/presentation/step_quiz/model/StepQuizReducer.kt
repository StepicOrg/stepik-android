package org.stepik.android.presentation.step_quiz.model

import org.stepik.android.presentation.step_quiz.StepQuizView.State
import org.stepik.android.presentation.step_quiz.StepQuizView.Message
import org.stepik.android.presentation.step_quiz.StepQuizView.Action
import org.stepik.android.presentation.step_quiz_review.reducer.StateReducer
import javax.inject.Inject

class StepQuizReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        TODO()
}