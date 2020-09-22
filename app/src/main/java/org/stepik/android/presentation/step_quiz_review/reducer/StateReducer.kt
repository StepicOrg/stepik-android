package org.stepik.android.presentation.step_quiz_review.reducer

interface StateReducer<State, Message, Action> {
    fun reduce(state: State, message: Message): Pair<State, Set<Action>>
}