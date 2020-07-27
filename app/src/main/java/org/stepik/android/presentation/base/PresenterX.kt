package org.stepik.android.presentation.base

import androidx.annotation.CallSuper
import ru.nobird.android.presentation.base.DisposableViewModel
import ru.nobird.android.presentation.base.PresenterContract
import java.util.ArrayDeque

abstract class PresenterX<State, Message, Action>(
    initState: State,
    private val reducer: StateReducer<State, Message, Action>
) : DisposableViewModel(), PresenterContract<View<State, Action>> {
    private var state: State = initState
        set(value) {
            field = value
            view?.render(value)
        }

    private var actionQueue = ArrayDeque<Action>()

    @Volatile
    var view: View<State, Action>? = null
        private set

    @CallSuper
    override fun attachView(view: View<State, Action>) {
        val previousView = this.view

        check(previousView == null) { "Previous view is not detached! previousView = $previousView" }

        this.view = view
        view.render(state)
        while (actionQueue.isNotEmpty()) {
            view.perform(actionQueue.pollFirst())
        }
    }

    @CallSuper
    override fun detachView(view: View<State, Action>) {
        val previousView = this.view

        if (previousView === view) {
            this.view = null
        } else {
            throw IllegalStateException("Unexpected view! previousView = $previousView, getView to unbind = $view")
        }
    }

    open fun onNewMessage(message: Message) {
        val (newState, actions) = reducer.reduce(state, message)
        state = newState

        val view = view
        if (view != null) {
            actions.forEach(view::perform)
        } else {
            actionQueue.addAll(actions)
        }
    }
}

interface StateReducer<State, Message, Action> {
    fun reduce(state: State, message: Message): Pair<State, Set<Action>>
}

interface View<State, Action> {
    fun render(state: State)
    fun perform(action: Action)
}