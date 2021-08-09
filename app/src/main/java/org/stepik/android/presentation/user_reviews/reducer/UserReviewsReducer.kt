package org.stepik.android.presentation.user_reviews.reducer

import org.stepik.android.presentation.user_reviews.UserReviewsFeature.State
import org.stepik.android.presentation.user_reviews.UserReviewsFeature.Message
import org.stepik.android.presentation.user_reviews.UserReviewsFeature.Action
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class UserReviewsReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage -> {
                if (state is State.Idle || state is State.Error && message.forceUpdate) {
                    State.Loading to setOf(Action.FetchUserReviews)
                } else {
                    null
                }
            }

            is Message.InitListeningMessage -> {
                if (state is State.Idle) {
                    State.Loading to setOf(Action.ListenForUserReviews)
                } else {
                    null
                }
            }

            is Message.FetchUserReviewsSuccess -> {
                if (state is State.Loading) {
                    State.Content(message.userCourseReviewItems) to emptySet()
                } else {
                    null
                }
            }

            is Message.FetchUserReviewsError -> {
                if (state is State.Loading) {
                    State.Error to emptySet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}