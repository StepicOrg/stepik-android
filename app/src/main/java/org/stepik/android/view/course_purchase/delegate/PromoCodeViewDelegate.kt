package org.stepik.android.view.course_purchase.delegate

import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.text.Editable
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import org.stepic.droid.R
import org.stepic.droid.databinding.BottomSheetDialogCoursePurchaseBinding
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import org.stepik.android.view.course.resolver.CoursePromoCodeResolver
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature.PromoCodeState
import org.stepik.android.view.step_quiz_choice.ui.delegate.LayerListDrawableDelegate

class PromoCodeViewDelegate(
    coursePurchaseBinding: BottomSheetDialogCoursePurchaseBinding,
    private val coursePurchaseData: CoursePurchaseData,
    private val displayPriceMapper: DisplayPriceMapper,
    private val promoCodeResolver: CoursePromoCodeResolver
) {
    private val context = coursePurchaseBinding.root.context
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
        coursePromoCodeInput.doAfterTextChanged { text: Editable? ->
            val length = text?.length ?: 0
            coursePromoCodeDismiss.isVisible = length != 0
            coursePromoCodeSubmitAction.isVisible = length != 0
        }
        coursePromoCodeDismiss.setOnClickListener { coursePromoCodeInput.setText("") }
    }

    fun render(state: PromoCodeState) {
        val (_, currencyCode, promoPrice, hasPromo) = promoCodeResolver.resolvePromoCodeInfo(
            coursePurchaseData.deeplinkPromoCode,
            coursePurchaseData.defaultPromoCode,
            coursePurchaseData.course
        )

        val courseDisplayPrice = coursePurchaseData.course.displayPrice

        coursePurchaseBuyAction.text =
            if (courseDisplayPrice != null) {
                if (hasPromo) {
                    displayPriceMapper.mapToDiscountedDisplayPriceSpannedString(courseDisplayPrice, currencyCode, promoPrice)
                } else {
                    context.getString(R.string.course_payments_purchase_in_web_with_price, courseDisplayPrice)
                }
            } else {
                context.getString(R.string.course_payments_purchase_in_web)
            }

        layerListDrawableDelegate.showLayer(getBackgroundLayer(state))
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