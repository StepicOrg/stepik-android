package org.stepik.android.presentation.learning_actions

import org.stepik.android.presentation.wishlist.WishlistFeature

interface LearningActionsFeature {
    data class State(
        val wishlistState: WishlistFeature.State
    )

    sealed class Message {
        /**
         * Message Wrappers
         */
        data class WishlistMessage(val message: WishlistFeature.Message) : Message()
    }

    sealed class Action {
        /**
         * Action Wrappers
         */
        data class WishlistAction(val action: WishlistFeature.Action) : Action()

        sealed class ViewAction : Action()
    }
}