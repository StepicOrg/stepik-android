package org.stepik.android.presentation.course_purchase

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.domain.course_purchase.model.CoursePurchaseObfuscatedParams
import org.stepik.android.domain.wishlist.model.WishlistOperationData
import org.stepik.android.model.Course
import org.stepik.android.presentation.course.model.EnrollmentError
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData

interface CoursePurchaseFeature {
    sealed class State {
        object Idle : State()
        data class Content(
            val coursePurchaseData: CoursePurchaseData,
            val paymentState: PaymentState,
            val promoCodeState: PromoCodeState,
            val wishlistState: WishlistState
        ) : State()
    }

    sealed class Message {
        data class InitMessage(val coursePurchaseData: CoursePurchaseData) : Message()

        object LaunchPurchaseFlow : Message()
        data class LaunchPurchaseFlowSuccess(val obfuscatedParams: CoursePurchaseObfuscatedParams, val skuDetails: SkuDetails) : Message()
        data class LaunchPurchaseFlowFailure(val enrollmentError: EnrollmentError) : Message()

        data class PurchaseFlowBillingSuccess(val purchase: Purchase) : Message()
        data class PurchaseFlowBillingFailure(val enrollmentError: EnrollmentError) : Message()

        object ConsumePurchaseSuccess : Message()
        data class ConsumePurchaseFailure(val enrollmentError: EnrollmentError) : Message()

        object LaunchRestorePurchaseFlow : Message()
        data class LaunchRestorePurchaseSuccess(val skuDetails: SkuDetails, val purchase: Purchase) : Message()
        data class LaunchRestorePurchaseFailure(val enrollmentError: EnrollmentError) : Message()

        object RestorePurchaseSuccess : Message()
        data class RestorePurchaseFailure(val skuDetails: SkuDetails, val purchase: Purchase, val enrollmentError: EnrollmentError) : Message()

        object StartLearningMessage : Message()

        /**
         * Wishlist messages
         */
        object WishlistAddMessage : Message()
        object WishlistAddSuccess : Message()
        object WishlistAddFailure : Message()

        /**
         * PromoCode
         */
        object PromoCodeEditingMessage : Message()
        data class PromoCodeCheckMessage(val text: String) : Message()
        data class PromoCodeValidMessage(val promoCodeSku: PromoCodeSku) : Message()
        object PromoCodeInvalidMessage : Message()
    }

    sealed class Action {
        data class AddToWishlist(
            val course: Course,
            val wishlistOperationData: WishlistOperationData
        ) : Action()

        data class CheckPromoCode(val courseId: Long, val promoCodeName: String) : Action()
        data class FetchLaunchFlowData(val courseId: Long, val skuId: String) : Action()
        data class ConsumePurchaseAction(val courseId: Long, val skuDetails: SkuDetails, val purchase: Purchase, val promoCode: String?) : Action()

        data class RestorePurchaseWithSkuId(val skuId: String) : Action()
        data class RestorePurchase(val courseId: Long, val skuDetails: SkuDetails, val purchase: Purchase) : Action()

        sealed class ViewAction : Action() {
            data class LaunchPurchaseFlowBilling(val obfuscatedParams: CoursePurchaseObfuscatedParams, val skuDetails: SkuDetails) : ViewAction()
            data class Error(val error: EnrollmentError) : ViewAction()

            object ShowLoading : Action.ViewAction()
            object ShowConsumeSuccess : Action.ViewAction()
            object ShowConsumeFailure : Action.ViewAction()
            object StartStudyAction : Action.ViewAction()
        }
    }

    sealed class PaymentState {
        object Idle : PaymentState()
        object ProcessingInitialCheck : PaymentState()
        data class ProcessingBillingPayment(val obfuscatedParams: CoursePurchaseObfuscatedParams, val skuDetails: SkuDetails) : PaymentState()
        data class ProcessingConsume(val skuDetails: SkuDetails, val purchase: Purchase) : PaymentState()

//        data class RestoringPayment(val skuDetails: SkuDetails, val purchase: Purchase) : PaymentState()

        object PaymentSuccess : PaymentState()
        data class PaymentFailure(val skuDetails: SkuDetails, val purchase: Purchase) : PaymentState()
    }

    sealed class PromoCodeState {
        object Idle : PromoCodeState()
        object Editing : PromoCodeState()
        data class Checking(val text: String) : PromoCodeState()
        data class Valid(val text: String, val promoCodeSku: PromoCodeSku) : PromoCodeState()
        object Invalid : PromoCodeState()
    }

    sealed class WishlistState {
        object Idle : WishlistState()
        object Adding : WishlistState()
        object Wishlisted : WishlistState()
    }
}