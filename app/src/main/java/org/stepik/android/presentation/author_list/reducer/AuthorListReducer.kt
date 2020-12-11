package org.stepik.android.presentation.author_list.reducer

import org.stepik.android.presentation.author_list.AuthorListFeature.State
import org.stepik.android.presentation.author_list.AuthorListFeature.Message
import org.stepik.android.presentation.author_list.AuthorListFeature.Action
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class AuthorListReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage ->
                if (state is State.Idle) {
                    State.Content(message.authorList.content) to emptySet()
                } else {
                    null
                }
        } ?: state to emptySet()
}