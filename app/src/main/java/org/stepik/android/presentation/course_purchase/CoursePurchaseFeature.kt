package org.stepik.android.presentation.course_purchase

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.domain.course_purchase.error.BillingException
import org.stepik.android.domain.course_purchase.model.CoursePurchaseObfuscatedParams
import org.stepik.android.domain.feedback.model.SupportEmailData
import org.stepik.android.presentation.course.model.EnrollmentError
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData
import org.stepik.android.presentation.wishlist.WishlistOperationFeature

interface CoursePurchaseFeature {
    sealed class State {
        object Idle : State()
        data class Content(
            val coursePurchaseData: CoursePurchaseData,
            val coursePurchaseSource: String,
            val paymentState: PaymentState,
            val promoCodeState: PromoCodeState,
            val wishlistState: WishlistOperationFeature.State
        ) : State()
    }

    sealed class Message {
        data class InitMessage(val coursePurchaseData: CoursePurchaseData, val coursePurchaseSource: String) : Message()

        object LaunchPurchaseFlow : Message()
        data class LaunchPurchaseFlowSuccess(val obfuscatedParams: CoursePurchaseObfuscatedParams, val skuDetails: SkuDetails) : Message()
        data class LaunchPurchaseFlowFailure(val throwable: Throwable) : Message()

        data class PurchaseFlowBillingSuccess(val purchases: List<Purchase>) : Message()
        data class PurchaseFlowBillingFailure(val billingException: BillingException) : Message()

        object ConsumePurchaseSuccess : Message()
        data class ConsumePurchaseFailure(val throwable: Throwable) : Message()

        data class LaunchRestorePurchaseFlow(val restoreCoursePurchaseSource: String) : Message()

        object RestorePurchaseSuccess : Message()
        data class RestorePurchaseFailure(val throwable: Throwable) : Message()

        object StartLearningMessage : Message()

        data class SetupFeedback(val subject: String, val deviceInfo: String) : Message()
        data class SetupFeedbackSuccess(val supportEmailData: SupportEmailData) : Message()

        /**
         * PromoCode
         */
        object HavePromoCodeMessage : Message()
        object PromoCodeEditingMessage : Message()
        data class PromoCodeCheckMessage(val text: String) : Message()
        data class PromoCodeValidMessage(val promoCodeSku: PromoCodeSku) : Message()
        object PromoCodeInvalidMessage : Message()

        /**
         * Wishlist message wrapper
         */
        data class WishlistMessage(val wishlistMessage: WishlistOperationFeature.Message) : Message()
    }

    sealed class Action {
        data class CheckPromoCode(val courseId: Long, val promoCodeName: String) : Action()
        data class FetchLaunchFlowData(val courseId: Long, val skuId: String) : Action()
        data class ConsumePurchaseAction(val courseId: Long, val skuDetails: SkuDetails, val purchase: Purchase, val promoCode: String?) : Action()

        data class RestorePurchase(val courseId: Long) : Action()

        data class GenerateSupportEmailData(val subject: String, val deviceInfo: String) : Action()

        data class LogAnalyticEvent(val analyticEvent: AnalyticEvent) : Action()

        /**
         * Wishlist action wrapper
         */
        data class WishlistAction(val action: WishlistOperationFeature.Action) : Action()

        sealed class ViewAction : Action() {
            data class LaunchPurchaseFlowBilling(val obfuscatedParams: CoursePurchaseObfuscatedParams, val skuDetails: SkuDetails) : ViewAction()
            data class Error(val error: EnrollmentError) : ViewAction()

            object ShowLoading : Action.ViewAction()
            object ShowConsumeSuccess : Action.ViewAction()
            object ShowConsumeFailure : Action.ViewAction()
            object StartStudyAction : Action.ViewAction()
            data class ShowContactSupport(val supportEmailData: SupportEmailData) : Action.ViewAction()
        }
    }

    sealed class PaymentState {
        object Idle : PaymentState()
        object ProcessingInitialCheck : PaymentState()
        data class ProcessingBillingPayment(val obfuscatedParams: CoursePurchaseObfuscatedParams, val skuDetails: SkuDetails) : PaymentState()
        data class ProcessingConsume(val skuDetails: SkuDetails, val purchase: Purchase) : PaymentState()

        object PaymentSuccess : PaymentState()
        object PaymentFailure : PaymentState()
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