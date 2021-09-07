package org.stepik.android.presentation.user_reviews.reducer

import org.stepik.android.presentation.user_reviews.UserReviewsFeature.State
import org.stepik.android.presentation.user_reviews.UserReviewsFeature.Message
import org.stepik.android.presentation.user_reviews.UserReviewsFeature.Action
import org.stepik.android.presentation.user_reviews.mapper.UserReviewsStateMapper
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class UserReviewsReducer
@Inject
constructor(
    private val userReviewsStateMapper: UserReviewsStateMapper
) : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage -> {
                if (state is State.Idle || state is State.Error && message.forceUpdate) {
                    State.Loading to setOf(Action.FetchUserReviews)
                } else {
                    null
                }
            }

            is Message.FetchUserReviewsSuccess -> {
                if (state !is State.Idle) {
                    val newState =
                        if (message.userCourseReviewsResult.userCourseReviewItems.isEmpty()) {
                            State.Empty
                        } else {
                            State.Content(message.userCourseReviewsResult)
                        }
                    newState to emptySet()
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
                        newState to emptySet()
                    }
                } else {
                    null
                }
            }

            is Message.EditReviewSubmission -> {
                if (state is State.Content) {
                    userReviewsStateMapper.mergeStateWithEditedReview(state, message.courseReview)?.let { newState ->
                        newState to emptySet()
                    }
                } else {
                    null
                }
            }

            is Message.DeletedReviewSubmission -> {
                if (state is State.Content) {
                    userReviewsStateMapper.mergeStateWithDeletedReview(state, message.courseReview)?.let { newState ->
                        newState to emptySet()
                    }
                } else {
                    null
                }
            }

            is Message.DeletedReviewUserReviews -> {
                if (state is State.Content) {
                    userReviewsStateMapper.mergeStateWithDeletedReviewPlaceholder(state, message.courseReview)?.let { newState ->
                        newState to setOf(Action.DeleteReview(message.courseReview))
                    }
                } else {
                    null
                }
            }

            is Message.DeletedReviewUserReviewsSuccess -> {
                if (state is State.Content) {
                    userReviewsStateMapper.mergeStateWithDeletedReviewToSuccess(state, message.courseReview)?.let { newState ->
                        newState to setOf(Action.ViewAction.ShowDeleteSuccessSnackbar)
                    }
                } else {
                    null
                }
            }

            is Message.DeletedReviewUserReviewsError -> {
                if (state is State.Content) {
                    val newState = userReviewsStateMapper.mergeStateWithDeletedReviewToError(state)
                    newState to setOf(Action.ViewAction.ShowDeleteFailureSnackbar)
                } else {
                    null
                }
            }

            is Message.EnrolledCourseMessage -> {
                if (state is State.Content) {
                    if (message.course.enrollment != 0L) {
                        state to setOf(Action.FetchEnrolledCourseInfo(message.course))
                    } else {
                        val newState = userReviewsStateMapper.mergeStateWithDroppedCourse(state, message.course.id)
                        newState to emptySet()
                    }
                } else {
                    null
                }
            }

            is Message.EnrolledReviewedCourseMessage -> {
                if (state is State.Content) {
                    val newState = userReviewsStateMapper.mergeStateWithEnrolledReviewedItem(state, message.reviewedItem)
                    newState to emptySet()
                } else {
                    null
                }
            }

            is Message.EnrolledPotentialReviewMessage -> {
                if (state is State.Content) {
                    val newState = userReviewsStateMapper.mergeStateWithEnrolledPotentialReviewItem(state, message.potentialReviewItem)
                    newState to emptySet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}