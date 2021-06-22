package org.stepik.android.presentation.wishlist

import org.stepik.android.domain.wishlist.model.WishlistEntity
import org.stepik.android.domain.wishlist.model.WishlistOperationData

interface WishlistFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Empty : State()
        object Error : State()

        data class Content(val wishListCourses: List<Long>) : State()
    }

    sealed class Message {
        data class InitMessage(val forceUpdate: Boolean = false) : Message()
        data class FetchWishlistSuccess(val wishlistEntity: WishlistEntity) : Message()
        object FetchWishListError : Message()
        data class WishlistOperationUpdate(val wishlistOperationData: WishlistOperationData) : Message()
    }

    sealed class Action {
        object FetchWishList : Action()
        sealed class ViewAction : Action()
    }
}