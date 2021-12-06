package org.stepik.android.view.course_purchase.delegate

import android.graphics.PorterDuff
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import org.stepic.droid.R
import org.stepic.droid.databinding.BottomSheetDialogCoursePurchaseBinding
import org.stepic.droid.util.resolveColorAttribute
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import ru.nobird.android.view.base.ui.extension.getDrawableCompat

class BuyActionViewDelegate(
    coursePurchaseBinding: BottomSheetDialogCoursePurchaseBinding,
    private val coursePurchaseData: CoursePurchaseData,
    private val displayPriceMapper: DisplayPriceMapper,
    launchPurchaseFlowAction: () -> Unit
) {
    private val context = coursePurchaseBinding.root.context
    private val coursePurchaseBuyAction = coursePurchaseBinding.coursePurchaseBuyAction
    private val coursePurchasePaymentIcon = coursePurchaseBinding.coursePurchasePaymentIcon
    private val coursePurchasePaymentTitle = coursePurchaseBinding.coursePurchasePaymentTitle
    private val coursePurchasePaymentFeedback = coursePurchaseBinding.coursePurchasePaymentFeedback
    private val coursePurchaseCommissionNotice = coursePurchaseBinding.coursePurchaseCommissionNotice

    private var paymentState: CoursePurchaseFeature.PaymentState = CoursePurchaseFeature.PaymentState.Idle

    init {
        coursePurchaseBuyAction.setOnClickListener {
            when (paymentState) {
                is CoursePurchaseFeature.PaymentState.Idle -> {
                    launchPurchaseFlowAction()
                }
                is CoursePurchaseFeature.PaymentState.PaymentSuccess -> {
                    // no op
                }
                is CoursePurchaseFeature.PaymentState.PaymentFailure -> {
                    // no op
                }
            }
        }
    }

    fun render(state: CoursePurchaseFeature.State.Content) {
        this.paymentState = state.paymentState
        coursePurchasePaymentTitle.isVisible = state.paymentState is CoursePurchaseFeature.PaymentState.PaymentFailure || state.paymentState is CoursePurchaseFeature.PaymentState.PaymentSuccess
        coursePurchasePaymentIcon.isVisible = state.paymentState is CoursePurchaseFeature.PaymentState.PaymentFailure || state.paymentState is CoursePurchaseFeature.PaymentState.PaymentSuccess
        coursePurchasePaymentFeedback.isVisible = state.paymentState is CoursePurchaseFeature.PaymentState.PaymentFailure
        coursePurchaseCommissionNotice.isGone = state.paymentState is CoursePurchaseFeature.PaymentState.PaymentFailure || state.paymentState is CoursePurchaseFeature.PaymentState.PaymentSuccess

        when (state.paymentState) {
            is CoursePurchaseFeature.PaymentState.Idle -> {
                coursePurchaseBuyAction.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                val courseDisplayPrice = coursePurchaseData.course.displayPrice
                coursePurchaseBuyAction.text =
                    if (courseDisplayPrice != null) {
                        if (state.promoCodeState is CoursePurchaseFeature.PromoCodeState.Valid && state.promoCodeState.promoCodeSku.lightSku != null) {
                            displayPriceMapper.mapToDiscountedDisplayPriceSpannedString(coursePurchaseData.primarySku.price, state.promoCodeState.promoCodeSku.lightSku.price)
                        } else {
                            context.getString(R.string.course_payments_purchase_in_web_with_price, coursePurchaseData.primarySku.price)
                        }
                    } else {
                        context.getString(R.string.course_payments_purchase_in_web)
                    }
            }

            is CoursePurchaseFeature.PaymentState.ProcessingInitialCheck,
            is CoursePurchaseFeature.PaymentState.ProcessingBillingPayment,
            is CoursePurchaseFeature.PaymentState.ProcessingConsume -> {
                val evaluationDrawable = AnimationDrawable()
                evaluationDrawable.addFrame(context.getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_1), 200)
                evaluationDrawable.addFrame(context.getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_2), 200)
                evaluationDrawable.addFrame(context.getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_3), 200)
                evaluationDrawable.isOneShot = false
                DrawableCompat.setTint(evaluationDrawable, context.resolveColorAttribute(R.attr.colorOnPrimary))
                coursePurchaseBuyAction.setCompoundDrawablesWithIntrinsicBounds(evaluationDrawable, null, null, null)
                coursePurchaseBuyAction.text = context.getString(R.string.course_purchase_payment_processing)
                evaluationDrawable.start()
            }

            is CoursePurchaseFeature.PaymentState.PaymentSuccess -> {
                coursePurchaseBuyAction.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                AppCompatResources
                    .getDrawable(context, R.drawable.ic_step_quiz_choice_correct)
                    ?.mutate()
                    ?.let { DrawableCompat.wrap(it) }
                    ?.also {
                        DrawableCompat.setTint(it, context.resolveColorAttribute(R.attr.colorOnPrimary))
                        DrawableCompat.setTintMode(it, PorterDuff.Mode.SRC_IN)
                    }
                coursePurchaseBuyAction.text = context.getString(R.string.course_purchase_payment_learn_action)
                coursePurchasePaymentTitle.text = context.getString(R.string.course_purchase_payment_success)
                coursePurchasePaymentIcon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_purchase_success))
            }

            is CoursePurchaseFeature.PaymentState.PaymentFailure -> {
                coursePurchaseBuyAction.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                coursePurchaseBuyAction.text = context.getString(R.string.course_purchase_payment_restore_action)
                coursePurchasePaymentTitle.text = context.getString(R.string.course_purchase_payment_failure)
                coursePurchasePaymentIcon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_purchase_fail))
            }
        }
    }
}