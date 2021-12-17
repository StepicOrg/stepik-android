package org.stepik.android.presentation.course_purchase.reducer

import org.stepik.android.domain.course_payments.model.PromoCodeSku
import org.stepik.android.domain.course_purchase.analytic.BuyCourseIAPFlowFailureAnalyticEvent
import org.stepik.android.domain.course_purchase.analytic.BuyCourseIAPFlowStartAnalyticEvent
import org.stepik.android.domain.course_purchase.analytic.BuyCourseIAPFlowSuccessAnalyticEvent
import org.stepik.android.domain.course_purchase.analytic.BuyCoursePromoFailureAnalyticEvent
import org.stepik.android.domain.course_purchase.analytic.BuyCoursePromoStartPressedAnalyticEvent
import org.stepik.android.domain.course_purchase.analytic.BuyCoursePromoSuccessAnalyticEvent
import org.stepik.android.domain.course_purchase.analytic.BuyCourseVerificationFailureAnalyticEvent
import org.stepik.android.domain.course_purchase.analytic.BuyCourseVerificationSuccessAnalyticEvent
import org.stepik.android.domain.course_purchase.analytic.RestoreCoursePurchaseFailureAnalyticEvent
import org.stepik.android.domain.course_purchase.analytic.RestoreCoursePurchasePressedAnalyticEvent
import org.stepik.android.domain.course_purchase.analytic.RestoreCoursePurchaseSuccessAnalyticEvent
import org.stepik.android.presentation.course.mapper.toEnrollmentError
import org.stepik.android.presentation.course.model.EnrollmentError
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature.State
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature.Message
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature.Action
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature
import org.stepik.android.presentation.wishlist.WishlistOperationFeature
import org.stepik.android.presentation.wishlist.reducer.WishlistOperationReducer
import ru.nobird.android.presentation.redux.reducer.StateReducer
import javax.inject.Inject

class CoursePurchaseReducer
@Inject
constructor(
    private val wishlistOperationReducer: WishlistOperationReducer
) : StateReducer<State, Message, Action> {
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
                        WishlistOperationFeature.State.Wishlisted
                    } else {
                        WishlistOperationFeature.State.Idle
                    }
                    State.Content(message.coursePurchaseData, message.coursePurchaseSource, CoursePurchaseFeature.PaymentState.Idle, promoCodeState, wishlistState) to emptySet()
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
                    state.copy(paymentState = CoursePurchaseFeature.PaymentState.ProcessingInitialCheck) to
                        setOf(Action.FetchLaunchFlowData(state.coursePurchaseData.course.id, skuId))
                } else {
                    null
                }
            }
            is Message.LaunchPurchaseFlowSuccess -> {
                if (state is State.Content && state.paymentState is CoursePurchaseFeature.PaymentState.ProcessingInitialCheck) {
                    state.copy(
                        paymentState = CoursePurchaseFeature.PaymentState.ProcessingBillingPayment(
                            message.obfuscatedParams,
                            message.skuDetails)
                    ) to setOf(
                        Action.ViewAction.LaunchPurchaseFlowBilling(message.obfuscatedParams, message.skuDetails),
                        Action.LogAnalyticEvent(
                            BuyCourseIAPFlowStartAnalyticEvent(
                                state.coursePurchaseData.course.id,
                                state.coursePurchaseSource,
                                state.coursePurchaseData.isWishlisted,
                                (state.promoCodeState as? CoursePurchaseFeature.PromoCodeState.Valid)?.text
                            )
                        )
                    )
                } else {
                    null
                }
            }
            is Message.LaunchPurchaseFlowFailure -> {
                if (state is State.Content && state.paymentState is CoursePurchaseFeature.PaymentState.ProcessingInitialCheck) {
                    state.copy(paymentState = CoursePurchaseFeature.PaymentState.Idle) to
                        setOf(Action.ViewAction.Error(message.throwable.toEnrollmentError()))
                } else {
                    null
                }
            }
            is Message.PurchaseFlowBillingSuccess -> {
                if (state is State.Content && state.paymentState is CoursePurchaseFeature.PaymentState.ProcessingBillingPayment) {
                    val promoCode = if (state.promoCodeState is CoursePurchaseFeature.PromoCodeState.Valid) {
                        state.promoCodeState.promoCodeSku.name
                    } else {
                        null
                    }

                    state.copy(paymentState = CoursePurchaseFeature.PaymentState.ProcessingConsume(state.paymentState.skuDetails, message.purchase)) to
                        setOf(
                            Action.ConsumePurchaseAction(
                                state.coursePurchaseData.course.id,
                                state.paymentState.skuDetails,
                                message.purchase,
                                promoCode
                            ),
                            Action.LogAnalyticEvent(
                                BuyCourseIAPFlowSuccessAnalyticEvent(
                                    state.coursePurchaseData.course.id,
                                    state.coursePurchaseSource,
                                    state.coursePurchaseData.isWishlisted,
                                    promoCode
                                )
                            )
                        )
                } else {
                    null
                }
            }
            is Message.PurchaseFlowBillingFailure -> {
                if (state is State.Content && state.paymentState is CoursePurchaseFeature.PaymentState.ProcessingBillingPayment) {
                    state.copy(paymentState = CoursePurchaseFeature.PaymentState.Idle) to
                        setOf(
                            Action.ViewAction.Error(message.billingException.toEnrollmentError()),
                            Action.LogAnalyticEvent(
                                BuyCourseIAPFlowFailureAnalyticEvent(
                                    state.coursePurchaseData.course.id,
                                    message.billingException.responseCode,
                                    message.billingException.errorMessage
                                )
                            )
                        )
                } else {
                    null
                }
            }
            is Message.ConsumePurchaseSuccess -> {
                if (state is State.Content && state.paymentState is CoursePurchaseFeature.PaymentState.ProcessingConsume) {
                    state.copy(paymentState = CoursePurchaseFeature.PaymentState.PaymentSuccess) to
                        setOf(
                            Action.ViewAction.ShowConsumeSuccess,
                            Action.LogAnalyticEvent(
                                BuyCourseVerificationSuccessAnalyticEvent(
                                    state.coursePurchaseData.course.id,
                                    state.coursePurchaseSource,
                                    state.coursePurchaseData.isWishlisted,
                                    (state.promoCodeState as? CoursePurchaseFeature.PromoCodeState.Valid)?.text
                                )
                            )
                        )
                } else {
                    null
                }
            }
            is Message.ConsumePurchaseFailure -> {
                if (state is State.Content && state.paymentState is CoursePurchaseFeature.PaymentState.ProcessingConsume) {
                    state.copy(paymentState = CoursePurchaseFeature.PaymentState.PaymentFailure(state.paymentState.skuDetails, state.paymentState.purchase)) to
                        setOf(
                            Action.LogAnalyticEvent(
                                BuyCourseVerificationFailureAnalyticEvent(
                                    state.coursePurchaseData.course.id,
                                    message.throwable.toEnrollmentError().name,
                                    message.throwable
                                )
                            )
                        )
                } else {
                    null
                }
            }
            is Message.LaunchRestorePurchaseFlow -> {
                if (state is State.Content) {
                    val skuId = if (state.promoCodeState is CoursePurchaseFeature.PromoCodeState.Valid) {
                        requireNotNull(state.promoCodeState.promoCodeSku.lightSku?.id)
                    } else {
                        state.coursePurchaseData.primarySku.id
                    }
                    state to setOf(
                        Action.ViewAction.ShowLoading,
                        Action.RestorePurchaseWithSkuId(skuId),
                        Action.LogAnalyticEvent(
                            RestoreCoursePurchasePressedAnalyticEvent(
                                state.coursePurchaseData.course.id,
                                message.restoreCoursePurchaseSource
                            )
                        )
                    )
                } else {
                    null
                }
            }
            is Message.LaunchRestorePurchaseSuccess -> {
                if (state is State.Content) {
                    state to setOf(Action.RestorePurchase(state.coursePurchaseData.course.id, message.skuDetails, message.purchase))
                } else {
                    null
                }
            }
            is Message.LaunchRestorePurchaseFailure -> {
                if (state is State.Content) {
                    state to setOf(Action.ViewAction.ShowConsumeFailure, Action.ViewAction.Error(message.throwable.toEnrollmentError()))
                } else {
                    null
                }
            }
            is Message.RestorePurchaseSuccess -> {
                if (state is State.Content) {
                    state.copy(paymentState = CoursePurchaseFeature.PaymentState.PaymentSuccess) to
                        setOf(
                            Action.ViewAction.ShowConsumeSuccess,
                            Action.LogAnalyticEvent(
                                RestoreCoursePurchaseSuccessAnalyticEvent(
                                    state.coursePurchaseData.course.id
                                )
                            )
                        )
                } else {
                    null
                }
            }
            is Message.RestorePurchaseFailure -> {
                if (state is State.Content) {
                    val enrollmentError = message.throwable.toEnrollmentError()
                    val analyticEventAction = Action.LogAnalyticEvent(
                        RestoreCoursePurchaseFailureAnalyticEvent(
                            state.coursePurchaseData.course.id,
                            enrollmentError.name,
                            message.throwable
                        )
                    )

                    if (enrollmentError == EnrollmentError.BILLING_NO_PURCHASES_TO_RESTORE) {
                        state to setOf(Action.ViewAction.ShowConsumeFailure, Action.ViewAction.Error(enrollmentError), analyticEventAction)
                    } else {
                        state.copy(paymentState = CoursePurchaseFeature.PaymentState.PaymentFailure(message.skuDetails, message.purchase)) to
                            setOf(Action.ViewAction.ShowConsumeFailure, analyticEventAction)
                    }
                } else {
                    null
                }
            }
            is Message.StartLearningMessage -> {
                if (state is State.Content && state.paymentState is CoursePurchaseFeature.PaymentState.PaymentSuccess) {
                    state to setOf(Action.ViewAction.StartStudyAction)
                } else {
                    null
                }
            }
            is Message.SetupFeedback -> {
                if (state is State.Content && state.paymentState is CoursePurchaseFeature.PaymentState.PaymentFailure) {
                    state to setOf(Action.GenerateSupportEmailData(message.subject, message.deviceInfo))
                } else {
                    null
                }
            }
            is Message.SetupFeedbackSuccess -> {
                if (state is State.Content && state.paymentState is CoursePurchaseFeature.PaymentState.PaymentFailure) {
                    state to setOf(Action.ViewAction.ShowContactSupport(message.supportEmailData))
                } else {
                    null
                }
            }
            is Message.HavePromoCodeMessage -> {
                if (state is State.Content) {
                    state.copy(promoCodeState = CoursePurchaseFeature.PromoCodeState.Editing) to
                        setOf(
                            Action.LogAnalyticEvent(
                                BuyCoursePromoStartPressedAnalyticEvent(
                                    state.coursePurchaseData.course
                                )
                            )
                        )
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
                    state.copy(promoCodeState = CoursePurchaseFeature.PromoCodeState.Checking(message.text)) to
                        setOf(Action.CheckPromoCode(state.coursePurchaseData.course.id, message.text))
                } else {
                    null
                }
            }
            is Message.PromoCodeValidMessage -> {
                if (state is State.Content && state.promoCodeState is CoursePurchaseFeature.PromoCodeState.Checking) {
                    state.copy(promoCodeState = CoursePurchaseFeature.PromoCodeState.Valid(state.promoCodeState.text, message.promoCodeSku)) to
                        setOf(
                            Action.LogAnalyticEvent(
                                BuyCoursePromoSuccessAnalyticEvent(
                                    state.coursePurchaseData.course,
                                    state.promoCodeState.text
                                )
                            )
                        )
                } else {
                    null
                }
            }
            is Message.PromoCodeInvalidMessage -> {
                if (state is State.Content && state.promoCodeState is CoursePurchaseFeature.PromoCodeState.Checking) {
                    state.copy(promoCodeState = CoursePurchaseFeature.PromoCodeState.Invalid) to
                        setOf(
                            Action.LogAnalyticEvent(
                                BuyCoursePromoFailureAnalyticEvent(
                                    state.coursePurchaseData.course,
                                    state.promoCodeState.text
                                )
                            )
                        )
                } else {
                    null
                }
            }
            is Message.WishlistMessage -> {
                if (state is State.Content) {
                    val (wishlistState, wishlistAction) = wishlistOperationReducer.reduce(state.wishlistState, message.wishlistMessage)
                    val newState = if (message.wishlistMessage is WishlistOperationFeature.Message.WishlistAddSuccess) {
                        state.copy(
                            coursePurchaseData = state.coursePurchaseData.copy(isWishlisted = true),
                            wishlistState = wishlistState
                        )
                    } else {
                        state.copy(wishlistState = wishlistState)
                    }
                    newState to wishlistAction.map(Action::WishlistAction).toSet()
                } else {
                    null
                }
            }
        } ?: state to emptySet()
}