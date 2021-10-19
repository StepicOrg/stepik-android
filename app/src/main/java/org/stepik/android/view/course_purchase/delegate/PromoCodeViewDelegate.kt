package org.stepik.android.view.course_purchase.delegate

import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.text.Editable
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import org.stepic.droid.R
import org.stepic.droid.databinding.BottomSheetDialogCoursePurchaseBinding
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import org.stepik.android.view.course.resolver.CoursePromoCodeResolver
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature.PromoCodeState
import org.stepik.android.presentation.course_purchase.CoursePurchaseViewModel
import org.stepik.android.view.step_quiz_choice.ui.delegate.LayerListDrawableDelegate

class PromoCodeViewDelegate(
    coursePurchaseBinding: BottomSheetDialogCoursePurchaseBinding,
    private val coursePurchaseViewModel: CoursePurchaseViewModel,
    private val coursePurchaseData: CoursePurchaseData,
    private val displayPriceMapper: DisplayPriceMapper,
    private val promoCodeResolver: CoursePromoCodeResolver
) {
    private val context = coursePurchaseBinding.root.context
    private val coursePromoCodeAction = coursePurchaseBinding.coursePromoCodeAction
    private val coursePromoCodeContainer = coursePurchaseBinding.coursePurchasePromoCodeInputContainer
    private val coursePromoCodeInput = coursePurchaseBinding.coursePurchasePromoCodeInput
    private val coursePromoCodeDismiss = coursePurchaseBinding.coursePurchasePromoCodeInputDismiss
    private val coursePromoCodeSubmitAction = coursePurchaseBinding.coursePurchasePromoCodeSubmitAction
    private val coursePurchaseBuyAction = coursePurchaseBinding.coursePurchaseBuyAction

    private val layerListDrawableDelegate = LayerListDrawableDelegate(
        listOf(
            R.id.idle_state,
            R.id.loading_state,
            R.id.invalid_state,
            R.id.valid_state
        ),
        (coursePromoCodeSubmitAction.background as RippleDrawable).findDrawableByLayerId(R.id.promo_code_layer_list) as LayerDrawable
    )

    init {
        coursePromoCodeAction.setOnClickListener {
            coursePurchaseViewModel.onNewMessage(CoursePurchaseFeature.Message.PromoCodeEditingMessage)
        }
        coursePromoCodeInput.doAfterTextChanged { text: Editable? ->
            val length = text?.length ?: 0
            coursePromoCodeDismiss.isVisible = length != 0
            coursePromoCodeSubmitAction.isVisible = length != 0
        }
        coursePromoCodeDismiss.setOnClickListener {
            coursePromoCodeInput.setText("")
            coursePurchaseViewModel.onNewMessage(CoursePurchaseFeature.Message.PromoCodeEditingMessage)
        }
        coursePromoCodeSubmitAction.setOnClickListener { coursePurchaseViewModel.onNewMessage(CoursePurchaseFeature.Message.PromoCodeCheckMessage(coursePromoCodeInput.text.toString())) }
    }

    fun render(state: PromoCodeState) {
        coursePromoCodeAction.isVisible = state is PromoCodeState.Idle
        coursePromoCodeContainer.isVisible = state !is PromoCodeState.Idle
        coursePromoCodeInput.isEnabled = state is PromoCodeState.Editing

        val courseDisplayPrice = coursePurchaseData.course.displayPrice

        coursePurchaseBuyAction.text =
            if (courseDisplayPrice != null) {
                if (state is PromoCodeState.Valid) {
                    displayPriceMapper.mapToDiscountedDisplayPriceSpannedString(courseDisplayPrice, state.coursePromoCodeInfo.currencyCode, state.coursePromoCodeInfo.price)
                } else {
                    context.getString(R.string.course_payments_purchase_in_web_with_price, courseDisplayPrice)
                }
            } else {
                context.getString(R.string.course_payments_purchase_in_web)
            }

        setEditTextFromState(state)
        layerListDrawableDelegate.showLayer(getBackgroundLayer(state))
    }

    private fun setEditTextFromState(state: PromoCodeState) {
        when (state) {
            is PromoCodeState.Checking ->
                coursePromoCodeInput.setText(state.text)
            is PromoCodeState.Valid ->
                coursePromoCodeInput.setText(state.text)
            else ->
                return
        }
    }

    private fun getBackgroundLayer(state: PromoCodeState): Int =
        when (state) {
            is PromoCodeState.Idle, is PromoCodeState.Editing ->
                R.id.idle_state
            is PromoCodeState.Checking ->
                R.id.loading_state
            is PromoCodeState.Invalid ->
                R.id.invalid_state
            is PromoCodeState.Valid ->
                R.id.valid_state
        }
}