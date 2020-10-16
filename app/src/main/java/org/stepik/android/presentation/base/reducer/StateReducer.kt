package org.stepik.android.presentation.base.reducer

interface StateReducer<State, Message, Action> {
    fun reduce(state: State, message: Message): Pair<State, Set<Action>>
}