package org.stepik.android.presentation.course_purchase.reducer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.stepik.android.domain.course.model.CourseStats
import org.stepik.android.domain.course.model.EnrollmentState
import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.domain.mobile_tiers.model.LightSku
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature.Action
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature.Message
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature.PaymentState
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature.PromoCodeState
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature.State
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData
import org.stepik.android.presentation.wishlist.WishlistOperationFeature
import org.stepik.android.presentation.wishlist.reducer.WishlistOperationReducer

class CoursePurchaseReducerTest {
    private val reducer = CoursePurchaseReducer(WishlistOperationReducer())

    @Test
    fun initMessageCreatesContentWithIdlePaymentAndWishlistIdleWhenNoPromoCode() {
        val (state, actions) = reducer.reduce(
            State.Idle,
            Message.InitMessage(coursePurchaseData(), COURSE_PURCHASE_SOURCE)
        )
        val contentState = state as State.Content

        assertTrue(actions.isEmpty())
        assertEquals(COURSE_ID, contentState.coursePurchaseData.course.id)
        assertSame(PaymentState.Idle, contentState.paymentState)
        assertSame(PromoCodeState.Idle, contentState.promoCodeState)
        assertSame(WishlistOperationFeature.State.Idle, contentState.wishlistState)
    }

    @Test
    fun initMessageCreatesContentWithPreloadedPromoCodeAndWishlistedState() {
        val promoCodeSku = promoCodeSku()
        val coursePurchaseData = coursePurchaseData(
            promoCodeSku = promoCodeSku,
            isWishlisted = true
        )

        val (state, actions) = reducer.reduce(
            State.Idle,
            Message.InitMessage(coursePurchaseData, COURSE_PURCHASE_SOURCE)
        )
        val contentState = state as State.Content
        val promoCodeState = contentState.promoCodeState as PromoCodeState.Valid

        assertTrue(actions.isEmpty())
        assertEquals(PROMO_CODE, promoCodeState.text)
        assertEquals(promoCodeSku, promoCodeState.promoCodeSku)
        assertSame(WishlistOperationFeature.State.Wishlisted, contentState.wishlistState)
    }

    @Test
    fun launchPurchaseFlowUsesPrimarySkuWhenPromoIsAbsent() {
        val (state, actions) = reducer.reduce(contentState(), Message.LaunchPurchaseFlow)
        val contentState = state as State.Content

        assertSame(PaymentState.ProcessingInitialCheck, contentState.paymentState)
        assertEquals(setOf(Action.FetchLaunchFlowData(COURSE_ID, PRIMARY_SKU_ID)), actions)
    }

    @Test
    fun launchPurchaseFlowUsesPromoSkuWhenPromoIsValid() {
        val promoCodeSku = promoCodeSku()
        val state = contentState(promoCodeState = PromoCodeState.Valid(PROMO_CODE, promoCodeSku))

        val (newState, actions) = reducer.reduce(state, Message.LaunchPurchaseFlow)
        val contentState = newState as State.Content

        assertSame(PaymentState.ProcessingInitialCheck, contentState.paymentState)
        assertEquals(setOf(Action.FetchLaunchFlowData(COURSE_ID, PROMO_SKU_ID)), actions)
    }

    @Test
    fun launchPurchaseFlowFailureReturnsPaymentStateToIdleAndShowsError() {
        val state = contentState(paymentState = PaymentState.ProcessingInitialCheck)

        val (newState, actions) = reducer.reduce(state, Message.LaunchPurchaseFlowFailure(RuntimeException()))
        val contentState = newState as State.Content

        assertSame(PaymentState.Idle, contentState.paymentState)
        assertTrue(actions.single() is Action.ViewAction.Error)
    }

    @Test
    fun billingSuccessWhilePaymentPendingStartsRestoreFlow() {
        val state = contentState(paymentState = PaymentState.PaymentPending)

        val (newState, actions) = reducer.reduce(state, Message.PurchaseFlowBillingSuccess(emptyList()))

        assertSame(state, newState)
        assertEquals(setOf(Action.ViewAction.ShowLoading, Action.RestorePurchase(COURSE_ID)), actions)
    }

    @Test
    fun launchRestorePurchaseFlowShowsLoadingAndStartsRestore() {
        val state = contentState()

        val (newState, actions) = reducer.reduce(state, Message.LaunchRestorePurchaseFlow(RESTORE_SOURCE))

        assertSame(state, newState)
        assertTrue(actions.contains(Action.ViewAction.ShowLoading))
        assertTrue(actions.contains(Action.RestorePurchase(COURSE_ID)))
        assertTrue(actions.any { it is Action.LogAnalyticEvent })
    }

    @Test
    fun restorePurchaseSuccessMarksPaymentSuccessAndShowsConsumeSuccess() {
        val state = contentState(paymentState = PaymentState.PaymentPending)

        val (newState, actions) = reducer.reduce(state, Message.RestorePurchaseSuccess)
        val contentState = newState as State.Content

        assertSame(PaymentState.PaymentSuccess, contentState.paymentState)
        assertTrue(actions.contains(Action.ViewAction.ShowConsumeSuccess))
        assertTrue(actions.any { it is Action.LogAnalyticEvent })
    }

    @Test
    fun launchPendingPurchaseFlowMarksPaymentPending() {
        val (state, actions) = reducer.reduce(contentState(), Message.LaunchPendingPurchaseFlow)
        val contentState = state as State.Content

        assertTrue(actions.isEmpty())
        assertSame(PaymentState.PaymentPending, contentState.paymentState)
    }

    @Test
    fun promoCodeCheckStartsValidationFromEditingState() {
        val state = contentState(promoCodeState = PromoCodeState.Editing)

        val (newState, actions) = reducer.reduce(state, Message.PromoCodeCheckMessage(PROMO_CODE))
        val contentState = newState as State.Content

        assertEquals(PromoCodeState.Checking(PROMO_CODE), contentState.promoCodeState)
        assertEquals(setOf(Action.CheckPromoCode(COURSE_ID, PROMO_CODE)), actions)
    }

    @Test
    fun promoCodeValidStoresFetchedPromoSku() {
        val promoCodeSku = promoCodeSku()
        val state = contentState(promoCodeState = PromoCodeState.Checking(PROMO_CODE))

        val (newState, actions) = reducer.reduce(state, Message.PromoCodeValidMessage(promoCodeSku))
        val contentState = newState as State.Content

        assertEquals(PromoCodeState.Valid(PROMO_CODE, promoCodeSku), contentState.promoCodeState)
        assertTrue(actions.single() is Action.LogAnalyticEvent)
    }

    @Test
    fun promoCodeInvalidMarksPromoStateInvalid() {
        val state = contentState(promoCodeState = PromoCodeState.Checking(PROMO_CODE))

        val (newState, actions) = reducer.reduce(state, Message.PromoCodeInvalidMessage)
        val contentState = newState as State.Content

        assertSame(PromoCodeState.Invalid, contentState.promoCodeState)
        assertTrue(actions.single() is Action.LogAnalyticEvent)
    }

    private fun contentState(
        paymentState: PaymentState = PaymentState.Idle,
        promoCodeState: PromoCodeState = PromoCodeState.Idle
    ): State.Content =
        State.Content(
            coursePurchaseData = coursePurchaseData(),
            coursePurchaseSource = COURSE_PURCHASE_SOURCE,
            paymentState = paymentState,
            promoCodeState = promoCodeState,
            wishlistState = WishlistOperationFeature.State.Idle
        )

    private fun coursePurchaseData(
        promoCodeSku: PromoCodeSku = PromoCodeSku.EMPTY,
        isWishlisted: Boolean = false
    ): CoursePurchaseData =
        CoursePurchaseData(
            course = Course(id = COURSE_ID),
            stats = CourseStats(
                review = 0.0,
                learnersCount = 0,
                readiness = 0.0,
                progress = null,
                enrollmentState = EnrollmentState.NotEnrolledWeb
            ),
            primarySku = LightSku(PRIMARY_SKU_ID, "$19.99"),
            promoCodeSku = promoCodeSku,
            isWishlisted = isWishlisted,
            purchaseState = UNSPECIFIED_PURCHASE_STATE
        )

    private fun promoCodeSku(): PromoCodeSku =
        PromoCodeSku(PROMO_CODE, LightSku(PROMO_SKU_ID, "$9.99"))

    private companion object {
        const val COURSE_ID = 100L
        const val PRIMARY_SKU_ID = "course_primary_sku"
        const val PROMO_SKU_ID = "course_promo_sku"
        const val PROMO_CODE = "PROMO"
        const val COURSE_PURCHASE_SOURCE = "course"
        const val RESTORE_SOURCE = "course_screen"
        const val UNSPECIFIED_PURCHASE_STATE = 0
    }
}
