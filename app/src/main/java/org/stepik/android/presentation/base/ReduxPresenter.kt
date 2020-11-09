package org.stepik.android.presentation.base

import androidx.lifecycle.ViewModel
import ru.nobird.android.presentation.redux.container.ReduxViewContainer
import ru.nobird.android.presentation.redux.container.ViewContainer

abstract class ReduxPresenter<State, Message, ViewAction>(
    private val viewContainer: ReduxViewContainer<State, Message, ViewAction>
) : ViewModel(), ViewContainer<State, Message, ViewAction> by viewContainer {
    override fun onCleared() {
        viewContainer.cancel()
    }
}
