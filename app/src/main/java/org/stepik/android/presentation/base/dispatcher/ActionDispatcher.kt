package org.stepik.android.presentation.base.dispatcher

import io.reactivex.disposables.CompositeDisposable

interface ActionDispatcher<Action, Message> {
    fun handleAction(action: Action, onNewMessage: (Message) -> Unit)
    val disposable: CompositeDisposable
}