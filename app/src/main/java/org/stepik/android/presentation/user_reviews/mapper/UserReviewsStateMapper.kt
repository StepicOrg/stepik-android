package org.stepik.android.presentation.user_reviews.mapper

import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.domain.user_reviews.model.UserCourseReviewItem
import org.stepik.android.presentation.user_reviews.UserReviewsFeature
import ru.nobird.android.core.model.mutate
import javax.inject.Inject

class UserReviewsStateMapper
@Inject
constructor() {
    /**
     * Operations from UserCourseReviewOperationBus
     */
    fun mergeStateWithNewReview(state: UserReviewsFeature.State.Content, courseReview: CourseReview): UserReviewsFeature.State.Content? {
        val indexOfReviewedCourse = state.userCourseReviewsResult.potentialReviewItems.indexOfFirst { it.id == courseReview.course }
        return if (indexOfReviewedCourse == -1) {
            null
        } else {
            val reviewedCourse = state.userCourseReviewsResult.potentialReviewItems[indexOfReviewedCourse]

            val updatedReviewedItems = state.userCourseReviewsResult.reviewedReviewItems.mutate { add(0, UserCourseReviewItem.ReviewedItem(reviewedCourse.course, courseReview)) }
            val updatedReviewedHeader = listOf(UserCourseReviewItem.ReviewedHeader(reviewedCount = updatedReviewedItems.size))

            val updatedPotentialReviews = state.userCourseReviewsResult.potentialReviewItems.mutate { removeAt(indexOfReviewedCourse) }
            val updatedPotentialReviewHeader = if (updatedPotentialReviews.isEmpty()) {
                emptyList()
            } else {
                listOf(UserCourseReviewItem.PotentialReviewHeader(potentialReviewCount = updatedPotentialReviews.size))
            }

            state.copy(
                userCourseReviewsResult = state.userCourseReviewsResult.copy(
                    userCourseReviewItems = updatedPotentialReviewHeader + updatedPotentialReviews + updatedReviewedHeader + updatedReviewedItems,
                    potentialHeader = updatedPotentialReviewHeader,
                    potentialReviewItems = updatedPotentialReviews,
                    reviewedHeader = updatedReviewedHeader,
                    reviewedReviewItems = updatedReviewedItems
                )
            )
        }
    }
    fun mergeStateWithEditedReview(state: UserReviewsFeature.State.Content, courseReview: CourseReview): UserReviewsFeature.State.Content? {
        val indexOfEditedReview = state.userCourseReviewsResult.reviewedReviewItems.indexOfFirst { it.id == courseReview.course }
        return if (indexOfEditedReview == -1) {
            null
        } else {
            val updatedReviewedItems = state
                .userCourseReviewsResult
                .reviewedReviewItems
                .mutate {
                    val oldItem = get(indexOfEditedReview)
                    set(indexOfEditedReview, oldItem.copy(courseReview = courseReview))
                }

            state.copy(
                userCourseReviewsResult = state.userCourseReviewsResult.copy(
                    userCourseReviewItems = with(state.userCourseReviewsResult) { potentialHeader + potentialReviewItems + reviewedHeader + updatedReviewedItems },
                    reviewedReviewItems = updatedReviewedItems
                )
            )
        }
    }

    fun mergeStateWithDeletedReview(state: UserReviewsFeature.State.Content, courseReview: CourseReview): UserReviewsFeature.State.Content? {
        val indexOfDeletedReview = state.userCourseReviewsResult.reviewedReviewItems.indexOfFirst { it.id == courseReview.course }
        return if (indexOfDeletedReview == -1) {
            null
        } else {
            val deletedReviewItem = state.userCourseReviewsResult.reviewedReviewItems[indexOfDeletedReview]

            val updatedReviewedItems = state.userCourseReviewsResult.reviewedReviewItems.mutate { removeAt(indexOfDeletedReview) }
            val updatedReviewedHeader = if (updatedReviewedItems.isEmpty()) {
                emptyList()
            } else {
                listOf(UserCourseReviewItem.ReviewedHeader(reviewedCount = updatedReviewedItems.size))
            }

            val updatedPotentialItems = state.userCourseReviewsResult.potentialReviewItems.mutate { add(UserCourseReviewItem.PotentialReviewItem(deletedReviewItem.course)) }
            val updatedPotentialReviewHeader = listOf(UserCourseReviewItem.PotentialReviewHeader(potentialReviewCount = updatedPotentialItems.size))

            state.copy(
                userCourseReviewsResult = state.userCourseReviewsResult.copy(
                    userCourseReviewItems = updatedPotentialReviewHeader + updatedPotentialItems + updatedReviewedHeader + updatedReviewedItems,
                    potentialHeader = updatedPotentialReviewHeader,
                    potentialReviewItems = updatedPotentialItems,
                    reviewedHeader = updatedReviewedHeader,
                    reviewedReviewItems = updatedReviewedItems
                )
            )
        }
    }

    /**
     * Operations from UserReviewsFragment screen
     */

    fun mergeStateWithDeletedReviewPlaceholder(state: UserReviewsFeature.State.Content, courseReview: CourseReview): UserReviewsFeature.State.Content? {
        val indexOfDeletedReview = state.userCourseReviewsResult.reviewedReviewItems.indexOfFirst { it.id == courseReview.course }
        return if (indexOfDeletedReview == -1) {
            null
        } else {
            val deletedReviewItem = state.userCourseReviewsResult.reviewedReviewItems[indexOfDeletedReview]

            val updatedReviewedItemsDisplay = (state.userCourseReviewsResult.reviewedReviewItems as List<UserCourseReviewItem>).mutate {
                set(indexOfDeletedReview, UserCourseReviewItem.Placeholder(courseId = deletedReviewItem.id, course = deletedReviewItem.course))
            }

            state.copy(
                userCourseReviewsResult = state.userCourseReviewsResult.copy(
                    userCourseReviewItems = with(state.userCourseReviewsResult) { potentialHeader + potentialReviewItems + reviewedHeader + updatedReviewedItemsDisplay }
                )
            )
        }
    }

    fun mergeStateWithDeletedReviewToSuccess(state: UserReviewsFeature.State.Content, courseReview: CourseReview): UserReviewsFeature.State.Content? {
        val indexOfPlaceholder = state.userCourseReviewsResult.userCourseReviewItems.indexOfFirst { it is UserCourseReviewItem.Placeholder && it.id == courseReview.course }
        return if (indexOfPlaceholder == -1) {
            null
        } else {
            val course = (state.userCourseReviewsResult.userCourseReviewItems[indexOfPlaceholder] as UserCourseReviewItem.Placeholder).course ?: return null
            val newPotentialReviewItem = UserCourseReviewItem.PotentialReviewItem(course = course)

            val updatedPotentialItems = state.userCourseReviewsResult.potentialReviewItems.mutate { add(newPotentialReviewItem) }
            val updatedPotentialReviewHeader = listOf(UserCourseReviewItem.PotentialReviewHeader(potentialReviewCount = updatedPotentialItems.size))

            val deletedReviewedItemIndex = state.userCourseReviewsResult.reviewedReviewItems.indexOfFirst { it.id == courseReview.course }
            val updatedReviewedItems =
                if (deletedReviewedItemIndex == -1) {
                    state.userCourseReviewsResult.reviewedReviewItems
                } else {
                    state.userCourseReviewsResult.reviewedReviewItems.mutate { removeAt(deletedReviewedItemIndex) }
                }
            val updatedReviewedHeader = if (updatedReviewedItems.isEmpty()) {
                emptyList()
            } else {
                listOf(UserCourseReviewItem.ReviewedHeader(updatedReviewedItems.size))
            }
            state.copy(
                userCourseReviewsResult = state.userCourseReviewsResult.copy(
                    userCourseReviewItems = updatedPotentialReviewHeader + updatedPotentialItems + updatedReviewedHeader + updatedReviewedItems,
                    potentialHeader = updatedPotentialReviewHeader,
                    potentialReviewItems = updatedPotentialItems,
                    reviewedHeader = updatedReviewedHeader,
                    reviewedReviewItems = updatedReviewedItems
                )
            )
        }
    }

    fun mergeStateWithDeletedReviewToError(state: UserReviewsFeature.State.Content): UserReviewsFeature.State.Content =
        state.copy(
            userCourseReviewsResult = state.userCourseReviewsResult.copy(
                userCourseReviewItems = with(state.userCourseReviewsResult) { potentialHeader + potentialReviewItems + reviewedHeader + reviewedReviewItems }
            )
        )

    // TODO APPS-3352 Return to this before merge
    fun mergeStateWithDroppedCourse(state: UserReviewsFeature.State.Content, droppedCourseId: Long): UserReviewsFeature.State.Content {
        val (updatedPotentialItems, updatedReviewedItems) = with(state.userCourseReviewsResult) {
            potentialReviewItems.filterNot { it.id == droppedCourseId } to reviewedReviewItems.filterNot { it.id == droppedCourseId }
        }
        val updatedPotentialHeader = if (updatedPotentialItems.isEmpty()) {
            emptyList()
        } else {
            listOf(UserCourseReviewItem.PotentialReviewHeader(updatedPotentialItems.size))
        }

        val updatedReviewedHeader = if (updatedReviewedItems.isEmpty()) {
            emptyList()
        } else {
            listOf(UserCourseReviewItem.ReviewedHeader(updatedReviewedItems.size))
        }

        return state.copy(
            userCourseReviewsResult = state.userCourseReviewsResult.copy(
                userCourseReviewItems = updatedPotentialHeader + updatedPotentialItems + updatedReviewedHeader + updatedReviewedItems,
                potentialHeader = updatedPotentialHeader,
                potentialReviewItems = updatedPotentialItems,
                reviewedHeader = updatedReviewedHeader,
                reviewedReviewItems = updatedReviewedItems
            )
        )
    }

    fun mergeStateWithEnrolledReviewedItem(state: UserReviewsFeature.State.Content, reviewedItem: UserCourseReviewItem.ReviewedItem): UserReviewsFeature.State.Content {
        val updatedReviewedItems = state.userCourseReviewsResult.reviewedReviewItems.mutate { add(reviewedItem) }
        val updatedReviewedHeader = listOf(UserCourseReviewItem.ReviewedHeader(updatedReviewedItems.size))
        return state.copy(
            userCourseReviewsResult = state.userCourseReviewsResult.copy(
                userCourseReviewItems = with(state.userCourseReviewsResult) { potentialHeader + potentialReviewItems + updatedReviewedHeader + updatedReviewedItems },
                reviewedHeader = updatedReviewedHeader,
                reviewedReviewItems = updatedReviewedItems
            )
        )
    }

    fun mergeStateWithEnrolledPotentialReviewItem(state: UserReviewsFeature.State.Content, potentialReviewItem: UserCourseReviewItem.PotentialReviewItem): UserReviewsFeature.State.Content {
        val updatedPotentialItems = state.userCourseReviewsResult.potentialReviewItems.mutate { add(potentialReviewItem) }
        val updatedPotentialHeader = listOf(UserCourseReviewItem.PotentialReviewHeader(updatedPotentialItems.size))
        return state.copy(
            userCourseReviewsResult = state.userCourseReviewsResult.copy(
                userCourseReviewItems = with(state.userCourseReviewsResult) { updatedPotentialHeader + updatedPotentialItems + reviewedHeader + reviewedReviewItems },
                potentialHeader = updatedPotentialHeader,
                potentialReviewItems = updatedPotentialItems
            )
        )
    }
}