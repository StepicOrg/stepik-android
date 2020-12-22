package org.stepik.android.presentation.story

import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepik.android.domain.story.model.StoryReaction
import org.stepik.android.presentation.story.StoryFeature.Message
import org.stepik.android.presentation.story.StoryFeature.Action
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class StoryActionDispatcher
@Inject
constructor(
    private val analytic: Analytic
) : RxActionDispatcher<Action, Message>() {
    override fun handleAction(action: Action) {
        when (action) {
            is Action.FetchVotes ->
                onNewMessage(Message.VoteFetchSuccess(emptyMap()))

            is Action.SaveReaction -> {
                analytic.reportAmplitudeEvent(AmplitudeAnalytic.Stories.STORY_REACTION_PRESSED, mapOf(
                    AmplitudeAnalytic.Stories.Values.STORY_ID to action.storyId,
                    AmplitudeAnalytic.Stories.Values.REACTION to action.reaction.analyticName,
                    AmplitudeAnalytic.Stories.Values.POSITION to action.storyPosition
                ))
            }
        }
    }

    private val StoryReaction.analyticName
        get() =
            when (this) {
                StoryReaction.LIKE -> "like"
                StoryReaction.DISLIKE -> "dislike"
            }
}