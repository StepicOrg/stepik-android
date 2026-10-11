package org.stepik.android.view.course_purchase.delegate

import android.graphics.PorterDuff
import android.graphics.drawable.AnimationDrawable
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import org.stepic.droid.R
import org.stepic.droid.databinding.BottomSheetDialogCoursePurchaseBinding
import org.stepic.droid.util.resolveColorAttribute
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import ru.nobird.android.view.base.ui.extension.getDrawableCompat

class BuyActionViewDelegate(
    private val binding: BottomSheetDialogCoursePurchaseBinding,
    private val coursePurchaseData: CoursePurchaseData,
    private val displayPriceMapper: DisplayPriceMapper,
    launchPurchaseFlowAction: () -> Unit,
    launchStartStudying: () -> Unit,
    launchRestoreAction: () -> Unit,
    closeDialog: () -> Unit
) {
    private var paymentState: CoursePurchaseFeature.PaymentState = CoursePurchaseFeature.PaymentState.Idle

    private val idleClickListener = View.OnClickListener {
        if (paymentState is CoursePurchaseFeature.PaymentState.Idle) {
            launchPurchaseFlowAction()
        }
    }

    init {
        binding.coursePurchaseBuyActionGreen.setOnClickListener(idleClickListener)
        binding.coursePurchaseBuyActionViolet.setOnClickListener(idleClickListener)
        binding.coursePurchaseTerminalAction.setOnClickListener {
            when (paymentState) {
                is CoursePurchaseFeature.PaymentState.PaymentSuccess -> {
                    launchStartStudying()
                }
                is CoursePurchaseFeature.PaymentState.PaymentFailure -> {
                    launchRestoreAction()
                }
                is CoursePurchaseFeature.PaymentState.PaymentPending -> {
                    closeDialog()
                }
                else -> {}
            }
        }
    }

    fun render(state: CoursePurchaseFeature.State.Content) {
        this.paymentState = state.paymentState
        val isTerminalState =
            state.paymentState is CoursePurchaseFeature.PaymentState.PaymentFailure ||
            state.paymentState is CoursePurchaseFeature.PaymentState.PaymentPending ||
            state.paymentState is CoursePurchaseFeature.PaymentState.PaymentSuccess

        binding.coursePurchasePaymentTitle.isVisible = isTerminalState
        binding.coursePurchasePaymentIcon.isVisible = isTerminalState
        binding.coursePurchasePaymentFailureFeedback.isVisible = state.paymentState is CoursePurchaseFeature.PaymentState.PaymentFailure
        binding.coursePurchasePaymentPendingFeedback.isVisible = state.paymentState is CoursePurchaseFeature.PaymentState.PaymentPending
        binding.coursePurchaseCommissionNotice.isGone = isTerminalState

        binding.coursePurchaseBuyActionGreen.isVisible = state.promoCodeState !is CoursePurchaseFeature.PromoCodeState.Valid && !isTerminalState
        binding.coursePurchaseBuyActionViolet.isVisible = state.promoCodeState is CoursePurchaseFeature.PromoCodeState.Valid && !isTerminalState
        binding.coursePurchaseTerminalAction.isVisible = isTerminalState
        renderIdleButton(state, binding.coursePurchaseBuyActionGreen)
        renderIdleButton(state, binding.coursePurchaseBuyActionViolet)
        renderTerminalButton(state)
    }

    private fun renderIdleButton(state: CoursePurchaseFeature.State.Content, coursePurchaseBuyAction: MaterialButton) {
        val context = binding.root.context

        when (state.paymentState) {
            is CoursePurchaseFeature.PaymentState.Idle -> {
                coursePurchaseBuyAction.icon = null
                val courseDisplayPrice = coursePurchaseData.course.displayPrice
                coursePurchaseBuyAction.text =
                    if (courseDisplayPrice != null) {
                        if (state.promoCodeState is CoursePurchaseFeature.PromoCodeState.Valid &&
                            state.promoCodeState.promoCodeSku.lightSku != null
                        ) {
                            displayPriceMapper.mapToDiscountedDisplayPriceSpannedString(
                                coursePurchaseData.primarySku.price,
                                state.promoCodeState.promoCodeSku.lightSku.price
                            )
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
                coursePurchaseBuyAction.icon = evaluationDrawable
                coursePurchaseBuyAction.text = context.getString(R.string.course_purchase_payment_processing)
                evaluationDrawable.start()
            }
            else -> Unit
        }
    }

    private fun renderTerminalButton(state: CoursePurchaseFeature.State.Content) {
        val context = binding.root.context

        when (state.paymentState) {
            is CoursePurchaseFeature.PaymentState.PaymentSuccess -> {
                val icon = AppCompatResources
                    .getDrawable(context, R.drawable.ic_purchase_success_check)
                    ?.mutate()
                    ?.let { DrawableCompat.wrap(it) }
                    ?.also {
                        DrawableCompat.setTint(it, context.resolveColorAttribute(R.attr.colorOnPrimary))
                        DrawableCompat.setTintMode(it, PorterDuff.Mode.SRC_IN)
                    }
                binding.coursePurchaseTerminalAction.icon = icon
                binding.coursePurchaseTerminalAction.text = context.getString(R.string.course_purchase_payment_learn_action)
                binding.coursePurchasePaymentTitle.text = context.getString(R.string.course_purchase_payment_success)
                binding.coursePurchasePaymentIcon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_purchase_success))
            }

            is CoursePurchaseFeature.PaymentState.PaymentFailure -> {
                binding.coursePurchaseTerminalAction.icon = null
                binding.coursePurchaseTerminalAction.text = context.getString(R.string.course_purchase_payment_restore_action)
                binding.coursePurchasePaymentTitle.text = context.getString(R.string.course_purchase_payment_failure)
                binding.coursePurchasePaymentIcon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_purchase_fail))
            }

            is CoursePurchaseFeature.PaymentState.PaymentPending -> {
                binding.coursePurchaseTerminalAction.icon = null
                binding.coursePurchaseTerminalAction.text = context.getString(R.string.course_purchase_payment_ok_action)
                binding.coursePurchasePaymentTitle.text = context.getString(R.string.course_purchase_payment_pending)
                binding.coursePurchasePaymentIcon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_purchase_pending))
            }
            else -> {}
        }
    }
}