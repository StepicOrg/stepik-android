package org.stepik.android.presentation.user_reviews.reducer

import org.stepik.android.presentation.user_reviews.UserReviewsFeature.State
import org.stepik.android.presentation.user_reviews.UserReviewsFeature.Message
import org.stepik.android.presentation.user_reviews.UserReviewsFeature.Action
import org.stepik.android.presentation.user_reviews.mapper.UserReviewsStateMapper
import org.stepik.android.view.injection.user_reviews.LearningActionsScope
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

@LearningActionsScope
class UserReviewsReducer
@Inject
constructor(
    private val userReviewsStateMapper: UserReviewsStateMapper
) : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage -> {
                if (state is State.Idle || state is State.Loading || state is State.Error && message.forceUpdate) {
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
                if (state is State.Loading || state is State.Content) {
                    State.Content(message.userCourseReviewsResult) to emptySet()
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

            is Message.NewReviewSubmission -> {
                if (state is State.Content) {
                    userReviewsStateMapper.mergeStateWithNewReview(state, message.courseReview)?.let { newState ->
                        newState to setOf(Action.PublishChanges(newState.userCourseReviewsResult))
                    }
                } else {
                    null
                }
            }

            is Message.EditReviewSubmission -> {
                if (state is State.Content) {
                    userReviewsStateMapper.mergeStateWithEditedReview(state, message.courseReview)?.let { newState ->
                        newState to setOf(Action.PublishChanges(newState.userCourseReviewsResult))
                    }
                } else {
                    null
                }
            }

            is Message.DeletedReview -> {
                if (state is State.Content) {
                    userReviewsStateMapper.mergeStateWithDeletedReview(state, message.courseReview)?.let { newState ->
                        newState to setOf(Action.PublishChanges(newState.userCourseReviewsResult), Action.DeleteReview(message.courseReview))
                    }
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}