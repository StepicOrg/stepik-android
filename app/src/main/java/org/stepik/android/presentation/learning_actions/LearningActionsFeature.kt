package org.stepik.android.presentation.learning_actions

import org.stepik.android.presentation.user_reviews.UserReviewsFeature
import org.stepik.android.presentation.wishlist.WishlistFeature

interface LearningActionsFeature {
    data class State(
        val wishlistState: WishlistFeature.State,
        val userReviewsState: UserReviewsFeature.State
    )

    sealed class Message {
        /**
         * Message Wrappers
         */
        data class WishlistMessage(val message: WishlistFeature.Message) : Message()
        data class UserReviewsMessage(val message: UserReviewsFeature.Message) : Message()
    }

    sealed class Action {
        /**
         * Action Wrappers
         */
        data class WishlistAction(val action: WishlistFeature.Action) : Action()
        data class UserReviewsAction(val action: UserReviewsFeature.Action) : Action()

        sealed class ViewAction : Action()
    }
}