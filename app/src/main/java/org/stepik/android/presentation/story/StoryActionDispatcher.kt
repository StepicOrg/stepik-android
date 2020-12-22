package org.stepik.android.presentation.story

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.story.interactor.StoryReactionInteractor
import org.stepik.android.domain.story.model.StoryReaction
import org.stepik.android.presentation.story.StoryFeature.Action
import org.stepik.android.presentation.story.StoryFeature.Message
import ru.nobird.android.domain.rx.emptyOnErrorStub
import ru.nobird.android.presentation.redux.dispatcher.RxActionDispatcher
import javax.inject.Inject

class StoryActionDispatcher
@Inject
constructor(
    private val analytic: Analytic,
    private val storyReactionInteractor: StoryReactionInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : RxActionDispatcher<Action, Message>() {
    override fun handleAction(action: Action) {
        when (action) {
            is Action.FetchVotes ->
                compositeDisposable += storyReactionInteractor
                    .getStoriesReactions()
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .onErrorReturnItem(emptyMap())
                    .subscribeBy(
                        onSuccess = { onNewMessage(Message.VoteFetchSuccess(it)) },
                        onError = emptyOnErrorStub
                    )

            is Action.SaveReaction -> {
                analytic.reportAmplitudeEvent(AmplitudeAnalytic.Stories.STORY_REACTION_PRESSED, mapOf(
                    AmplitudeAnalytic.Stories.Values.STORY_ID to action.storyId,
                    AmplitudeAnalytic.Stories.Values.REACTION to action.reaction.analyticName,
                    AmplitudeAnalytic.Stories.Values.POSITION to action.storyPosition
                ))

                compositeDisposable += storyReactionInteractor
                    .saveStoryReaction(action.storyId, action.reaction)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onError = emptyOnErrorStub
                    )
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