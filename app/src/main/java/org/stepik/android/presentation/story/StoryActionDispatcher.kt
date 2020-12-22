package org.stepik.android.presentation.story

import org.stepik.android.presentation.story.StoryFeature.Message
import org.stepik.android.presentation.story.StoryFeature.Action
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class StoryActionDispatcher
@Inject
constructor(

) : RxActionDispatcher<Action, Message>() {
    override fun handleAction(action: Action) {
        when (action) {
            is Action.FetchVotes ->
                onNewMessage(Message.VoteFetchSuccess(emptyMap()))

            is Action.SaveReaction -> {

            }
        }
    }
}