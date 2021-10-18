package org.stepik.android.presentation.course_purchase

import org.stepik.android.domain.wishlist.model.WishlistEntity
import org.stepik.android.domain.wishlist.model.WishlistOperationData
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData

interface CoursePurchaseFeature {
    sealed class State {
        object Idle : State()
        data class Content(
            val coursePurchaseData: CoursePurchaseData,
            val promoCodeState: PromoCodeState,
            val wishlistState: WishlistState
        ) : State()
    }

    sealed class Message {
        data class InitMessage(val coursePurchaseData: CoursePurchaseData) : Message()
        object WishlistAddMessage : Message()
        data class WishlistAddSuccess(val wishlistEntity: WishlistEntity) : Message()
        object WishlistAddFailure : Message()
    }

    sealed class Action {
        data class AddToWishlist(
            val course: Course,
            val wishlistEntity: WishlistEntity,
            val wishlistOperationData: WishlistOperationData
        ) : Action()
        sealed class ViewAction : Action()
    }

    sealed class PromoCodeState {
        object Idle : PromoCodeState()
        data class Editing(val text: String) : PromoCodeState()
        data class Checking(val text: String) : PromoCodeState()
        data class Valid(val text: String) : PromoCodeState()
        data class Invalid(val text: String) : PromoCodeState()
    }

    sealed class WishlistState {
        object Idle : WishlistState()
        object Adding : WishlistState()
        object Wishlisted : WishlistState()
    }
}