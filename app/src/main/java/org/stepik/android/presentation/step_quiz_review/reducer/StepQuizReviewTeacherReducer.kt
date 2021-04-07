package org.stepik.android.presentation.step_quiz_review.reducer

import org.stepik.android.presentation.step_quiz.reducer.StepQuizReducer
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewTeacherFeature.Action
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewTeacherFeature.Message
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewTeacherFeature.State
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class StepQuizReviewTeacherReducer
@Inject
constructor(
    private val stepQuizReducer: StepQuizReducer
) : StateReducer<State, Message, Action> {
    override fun reduce(
        state: State,
        message: Message
    ): Pair<State, Set<Action>> =
        when (message) {
            else -> null
        } ?: state to emptySet()
}
