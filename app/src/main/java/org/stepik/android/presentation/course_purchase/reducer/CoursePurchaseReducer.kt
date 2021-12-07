package org.stepik.android.presentation.course_purchase.reducer

import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.domain.wishlist.model.WishlistOperationData
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature.State
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature.Message
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature.Action
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature
import org.stepik.android.presentation.wishlist.model.WishlistAction
import ru.nobird.android.core.model.mutate
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class CoursePurchaseReducer
@Inject
constructor() : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.InitMessage -> {
                if (state is State.Idle) {
                    val promoCodeState = if (message.coursePurchaseData.promoCodeSku != PromoCodeSku.EMPTY) {
                        CoursePurchaseFeature.PromoCodeState.Valid(message.coursePurchaseData.promoCodeSku.name, message.coursePurchaseData.promoCodeSku)
                    } else {
                        CoursePurchaseFeature.PromoCodeState.Idle
                    }
                    val wishlistState = if (message.coursePurchaseData.isWishlisted) {
                        CoursePurchaseFeature.WishlistState.Wishlisted
                    } else {
                        CoursePurchaseFeature.WishlistState.Idle
                    }
                    State.Content(message.coursePurchaseData, CoursePurchaseFeature.PaymentState.Idle, promoCodeState, wishlistState) to emptySet()
                } else {
                    null
                }
            }
            is Message.LaunchPurchaseFlow -> {
                if (state is State.Content) {
                    val skuId = if (state.promoCodeState is CoursePurchaseFeature.PromoCodeState.Valid) {
                        requireNotNull(state.promoCodeState.promoCodeSku.lightSku?.id)
                    } else {
                        state.coursePurchaseData.primarySku.id
                    }
                    state.copy(paymentState = CoursePurchaseFeature.PaymentState.ProcessingInitialCheck) to setOf(Action.FetchLaunchFlowData(state.coursePurchaseData.course.id, skuId))
                } else {
                    null
                }
            }
            is Message.LaunchPurchaseFlowSuccess -> {
                if (state is State.Content && state.paymentState is CoursePurchaseFeature.PaymentState.ProcessingInitialCheck) {
                    state.copy(paymentState = CoursePurchaseFeature.PaymentState.ProcessingBillingPayment(message.obfuscatedParams, message.skuDetails)) to setOf(Action.ViewAction.LaunchPurchaseFlowBilling(message.obfuscatedParams, message.skuDetails))
                } else {
                    null
                }
            }
            is Message.LaunchPurchaseFlowFailure -> {
                if (state is State.Content && state.paymentState is CoursePurchaseFeature.PaymentState.ProcessingInitialCheck) {
                    state.copy(paymentState = CoursePurchaseFeature.PaymentState.Idle) to setOf(Action.ViewAction.Error(message.enrollmentError))
                } else {
                    null
                }
            }
            is Message.PurchaseFlowBillingSuccess -> {
                if (state is State.Content && state.paymentState is CoursePurchaseFeature.PaymentState.ProcessingBillingPayment) {
                    state.copy(paymentState = CoursePurchaseFeature.PaymentState.ProcessingConsume(state.paymentState.skuDetails, message.purchase)) to setOf(Action.ConsumePurchaseAction(state.coursePurchaseData.course.id, state.paymentState.skuDetails, message.purchase))
                } else {
                    null
                }
            }
            is Message.PurchaseFlowBillingFailure -> {
                if (state is State.Content && state.paymentState is CoursePurchaseFeature.PaymentState.ProcessingBillingPayment) {
                    state.copy(paymentState = CoursePurchaseFeature.PaymentState.Idle) to setOf(Action.ViewAction.Error(message.enrollmentError))
                } else {
                    null
                }
            }
            is Message.ConsumePurchaseSuccess -> {
                if (state is State.Content && state.paymentState is CoursePurchaseFeature.PaymentState.ProcessingConsume) {
                    state.copy(paymentState = CoursePurchaseFeature.PaymentState.PaymentSuccess) to emptySet()
                } else {
                    null
                }
            }
            is Message.ConsumePurchaseFailure -> {
                if (state is State.Content && state.paymentState is CoursePurchaseFeature.PaymentState.ProcessingConsume) {
                    state.copy(paymentState = CoursePurchaseFeature.PaymentState.PaymentFailure(state.paymentState.skuDetails, state.paymentState.purchase)) to setOf(Action.ViewAction.Error(message.enrollmentError))
                } else {
                    null
                }
            }
            is Message.RestorePurchase -> {
                if (state is State.Content) {
                    val skuId = if (state.promoCodeState is CoursePurchaseFeature.PromoCodeState.Valid) {
                        requireNotNull(state.promoCodeState.promoCodeSku.lightSku?.id)
                    } else {
                        state.coursePurchaseData.primarySku.id
                    }
                    state to setOf(Action.ViewAction.ShowLoading, Action.RestorePurchaseWithSkuId(state.coursePurchaseData.course.id, skuId))
                } else {
                    null
                }
            }
            is Message.RestorePurchaseSuccess -> {
                if (state is State.Content) {
                    state to setOf(Action.ViewAction.ShowConsumeSuccess)
                } else {
                    null
                }
            }
            is Message.RestorePurchaseFailure -> {
                if (state is State.Content) {
                    state to setOf(Action.ViewAction.ShowConsumeFailure, Action.ViewAction.Error(message.enrollmentError))
                } else {
                    null
                }
            }
            is Message.WishlistAddMessage -> {
                if (state is State.Content) {
                    val wishlistEntity = state.coursePurchaseData.wishlistEntity.copy(courses = state.coursePurchaseData.wishlistEntity.courses.mutate { add(0, state.coursePurchaseData.course.id) })
                    val wishlistOperationData = WishlistOperationData(state.coursePurchaseData.course.id, WishlistAction.ADD)
                    state.copy(wishlistState = CoursePurchaseFeature.WishlistState.Adding)to setOf(Action.AddToWishlist(state.coursePurchaseData.course, wishlistEntity, wishlistOperationData))
                } else {
                    null
                }
            }
            is Message.WishlistAddSuccess -> {
                if (state is State.Content) {
                    val updatedCoursePurchaseData = state.coursePurchaseData.copy(wishlistEntity = message.wishlistEntity, isWishlisted = true)
                    state.copy(coursePurchaseData = updatedCoursePurchaseData, wishlistState = CoursePurchaseFeature.WishlistState.Wishlisted) to emptySet()
                } else {
                    null
                }
            }
            is Message.WishlistAddFailure -> {
                if (state is State.Content) {
                    state.copy(wishlistState = CoursePurchaseFeature.WishlistState.Idle) to emptySet()
                } else {
                    null
                }
            }
            is Message.PromoCodeEditingMessage -> {
                if (state is State.Content) {
                    state.copy(promoCodeState = CoursePurchaseFeature.PromoCodeState.Editing) to emptySet()
                } else {
                    null
                }
            }
            is Message.PromoCodeCheckMessage -> {
                if (state is State.Content && state.promoCodeState is CoursePurchaseFeature.PromoCodeState.Editing) {
                    state.copy(promoCodeState = CoursePurchaseFeature.PromoCodeState.Checking(message.text)) to setOf(Action.CheckPromoCode(state.coursePurchaseData.course.id, message.text))
                } else {
                    null
                }
            }
            is Message.PromoCodeValidMessage -> {
                if (state is State.Content && state.promoCodeState is CoursePurchaseFeature.PromoCodeState.Checking) {
                    state.copy(promoCodeState = CoursePurchaseFeature.PromoCodeState.Valid(state.promoCodeState.text, message.promoCodeSku)) to emptySet()
                } else {
                    null
                }
            }
            is Message.PromoCodeInvalidMessage -> {
                if (state is State.Content && state.promoCodeState is CoursePurchaseFeature.PromoCodeState.Checking) {
                    state.copy(promoCodeState = CoursePurchaseFeature.PromoCodeState.Invalid) to emptySet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}