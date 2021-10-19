package org.stepik.android.presentation.course_purchase

import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.domain.wishlist.model.WishlistEntity
import org.stepik.android.domain.wishlist.model.WishlistOperationData
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData
import org.stepik.android.view.course.model.CoursePromoCodeInfo

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
        data class InitMessage(val coursePurchaseData: CoursePurchaseData, val initialCoursePromoCodeInfo: CoursePromoCodeInfo) : Message()

        /**
         * Wishlist messages
         */
        object WishlistAddMessage : Message()
        data class WishlistAddSuccess(val wishlistEntity: WishlistEntity) : Message()
        object WishlistAddFailure : Message()

        /**
         * PromoCode
         */
        object PromoCodeEditingMessage : Message()
        data class PromoCodeCheckMessage(val text: String) : Message()
        data class PromoCodeValidMessage(val deeplinkPromoCode: DeeplinkPromoCode) : Message()
        object PromoCodeInvalidMessage : Message()
    }

    sealed class Action {
        data class AddToWishlist(
            val course: Course,
            val wishlistEntity: WishlistEntity,
            val wishlistOperationData: WishlistOperationData
        ) : Action()

        data class CheckPromoCode(val courseId: Long, val promoCodeName: String) : Action()
        sealed class ViewAction : Action()
    }

    sealed class PromoCodeState {
        object Idle : PromoCodeState()
        object Editing : PromoCodeState()
        data class Checking(val text: String) : PromoCodeState()
        data class Valid(val text: String, val coursePromoCodeInfo: CoursePromoCodeInfo) : PromoCodeState()
        object Invalid : PromoCodeState()
    }

    sealed class WishlistState {
        object Idle : WishlistState()
        object Adding : WishlistState()
        object Wishlisted : WishlistState()
    }
}