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
                    val updatedReviewedHeader = if (state.userCourseReviewsResult.reviewedHeader.isEmpty()) {
                        listOf(UserCourseReviewItem.ReviewedHeader(reviewedCount = 1))
                    } else {
                        listOf(UserCourseReviewItem.ReviewedHeader(reviewedCount = state.userCourseReviewsResult.reviewedReviewItems.size + 1))
                    }

                    val indexOfReviewedCourse = state.userCourseReviewsResult.potentialReviewItems.indexOfFirst { it.id == message.courseReview.course }
                    val newState = if (indexOfReviewedCourse == -1) {
                        null
                    } else {
                        val reviewedCourse = state.userCourseReviewsResult.potentialReviewItems[indexOfReviewedCourse]
                        val updatedReviewedItems = state.userCourseReviewsResult.reviewedReviewItems.mutate { add(0, UserCourseReviewItem.ReviewedItem(reviewedCourse.course, message.courseReview)) }

                        val updatedPotentialReviews = state.userCourseReviewsResult.potentialReviewItems.mutate { removeAt(indexOfReviewedCourse) }
                        val potentialReviewHeader = if (updatedPotentialReviews.isEmpty()) {
                            emptyList()
                        } else {
                            listOf(UserCourseReviewItem.PotentialReviewHeader(potentialReviewCount = updatedPotentialReviews.size))
                        }
                        state.copy(
                            userCourseReviewsResult = state.userCourseReviewsResult.copy(
                                userCourseReviewItems = updatedReviewedHeader + updatedReviewedItems + potentialReviewHeader + updatedPotentialReviews,
                                reviewedHeader = updatedReviewedHeader,
                                reviewedReviewItems = updatedReviewedItems,
                                potentialHeader = potentialReviewHeader,
                                potentialReviewItems = updatedPotentialReviews
                            )
                        )
                    }
                    newState?.let { it to setOf(Action.PublishChanges(it.userCourseReviewsResult)) }
                } else {
                    null
                }
            }

            is Message.EditReviewSubmission -> {
                if (state is State.Content) {
                    val indexOf = state.userCourseReviewsResult.reviewedReviewItems.indexOfFirst { it.id == message.courseReview.course }
                    val newState = if (indexOf == -1) {
                        null
                    } else {
                        val updatedReviewedItems = state
                            .userCourseReviewsResult
                            .reviewedReviewItems
                            .mutate {
                                val oldItem = get(indexOf)
                                set(indexOf, oldItem.copy(courseReview = message.courseReview))
                            }
                        state.copy(
                            userCourseReviewsResult = state.userCourseReviewsResult.copy(
                                userCourseReviewItems = with(state.userCourseReviewsResult) { potentialHeader + potentialReviewItems + reviewedHeader + updatedReviewedItems },
                                reviewedReviewItems = updatedReviewedItems
                            )
                        )
                    }
                    newState?.let { it to setOf(Action.PublishChanges(it.userCourseReviewsResult)) }
                } else {
                    null
                }
            }

            is Message.DeletedReview -> {
                if (state is State.Content) {
                    val indexOfDeletedReview = state.userCourseReviewsResult.reviewedReviewItems.indexOfFirst { it.id == message.courseReview.course }
                    val newState = if (indexOfDeletedReview == -1) {
                        null
                    } else {
                        val deletedReviewItems = state.userCourseReviewsResult.reviewedReviewItems[indexOfDeletedReview]
                        val updatedReviewedItems = state.userCourseReviewsResult.reviewedReviewItems.mutate { removeAt(indexOfDeletedReview) }
                        val updatedReviewedHeader = if (updatedReviewedItems.isEmpty()) {
                            emptyList()
                        } else {
                            listOf(UserCourseReviewItem.ReviewedHeader(reviewedCount = updatedReviewedItems.size))
                        }
                        val updatedPotentialItems = state.userCourseReviewsResult.potentialReviewItems.mutate { add(UserCourseReviewItem.PotentialReviewItem(deletedReviewItems.course)) }
                        val updatedPotentialReviewHeader = listOf(UserCourseReviewItem.PotentialReviewHeader(potentialReviewCount = updatedPotentialItems.size))
                        state.copy(
                            userCourseReviewsResult = state.userCourseReviewsResult.copy(
                                userCourseReviewItems = updatedReviewedHeader + updatedReviewedItems + updatedPotentialReviewHeader + updatedPotentialItems,
                                reviewedHeader = updatedReviewedHeader,
                                reviewedReviewItems = updatedReviewedItems,
                                potentialHeader = updatedPotentialReviewHeader,
                                potentialReviewItems = updatedPotentialItems
                            )
                        )
                    }
                    newState?.let { it to setOf(Action.PublishChanges(it.userCourseReviewsResult), Action.DeleteReview(message.courseReview)) }
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}