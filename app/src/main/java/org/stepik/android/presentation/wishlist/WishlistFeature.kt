package org.stepik.android.presentation.wishlist

import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.domain.wishlist.model.WishlistWrapper

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
        data class FetchWishlistSuccess(val wishListRecord: StorageRecord<WishlistWrapper>) : Message()
        object FetchWishListError : Message()
    }

    sealed class Action {
        object FetchWishList : Action()
        sealed class ViewAction : Action()
    }
}