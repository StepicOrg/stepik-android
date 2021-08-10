package org.stepik.android.presentation.user_reviews.reducer

import org.stepik.android.domain.user_reviews.model.UserCourseReviewItem
import org.stepik.android.presentation.user_reviews.UserReviewsFeature.State
import org.stepik.android.presentation.user_reviews.UserReviewsFeature.Message
import org.stepik.android.presentation.user_reviews.UserReviewsFeature.Action
import org.stepik.android.view.injection.user_reviews.LearningActionsScope
import ru.nobird.android.core.model.mutate
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

@LearningActionsScope
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

            is Message.NewReviewSubmission -> {
                if (state is State.Content) {
//                    val indexOf = state.userCourseReviewItems.indexOfFirst { it is UserCourseReviewItem.ReviewedHeader }
//                    if (indexOf == -1) {
//                        state.copy(
//                            userCourseReviewItems = state.userCourseReviewItems + listOf(UserCourseReviewItem.ReviewedHeader(reviewedCount = 1), UserCourseReviewItem.ReviewedItem())
//                        )
//                    } else {
//
//                    }
                    null
                } else {
                    null
                }
            }

            is Message.EditReviewSubmission -> {
                if (state is State.Content) {
                    val indexOf = state.userCourseReviewItems.indexOfFirst { it is UserCourseReviewItem.ReviewedItem && it.id == message.courseReview.course }
                    val newState = if (indexOf == -1) {
                        null
                    } else {
                        state.copy(
                            userCourseReviewItems = state
                                .userCourseReviewItems
                                .mutate {
                                    val oldItem = get(indexOf) as UserCourseReviewItem.ReviewedItem
                                    set(indexOf, oldItem.copy(courseReview = message.courseReview))
                                }
                        )
                    }
                    newState?.let { it to setOf(Action.PublishChanges(it.userCourseReviewItems)) }
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}