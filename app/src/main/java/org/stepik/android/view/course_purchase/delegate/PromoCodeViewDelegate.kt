package org.stepik.android.view.course_purchase.delegate

import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.text.Editable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import org.stepic.droid.R
import org.stepic.droid.databinding.BottomSheetDialogCoursePurchaseBinding
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature.PromoCodeState
import org.stepik.android.presentation.course_purchase.CoursePurchaseViewModel
import org.stepik.android.view.step_quiz_choice.ui.delegate.LayerListDrawableDelegate
import ru.nobird.android.view.base.ui.extension.getDrawableCompat
import ru.nobird.android.view.base.ui.extension.setTextIfChanged

class PromoCodeViewDelegate(
    private val binding: BottomSheetDialogCoursePurchaseBinding,
    private val coursePurchaseViewModel: CoursePurchaseViewModel
) {
    companion object {
        private const val EVALUATION_FRAME_DURATION_MS = 250
    }

    private val layerListDrawableDelegate = LayerListDrawableDelegate(
        listOf(
            R.id.idle_state,
            R.id.loading_state,
            R.id.invalid_state,
            R.id.valid_state
        ),
        (binding.coursePurchasePromoCodeSubmitAction.background as RippleDrawable).findDrawableByLayerId(R.id.promo_code_layer_list) as LayerDrawable
    )

    private var promoCodeState: PromoCodeState = PromoCodeState.Idle

    init {
        binding.coursePromoCodeAction.setOnClickListener {
            coursePurchaseViewModel.onNewMessage(CoursePurchaseFeature.Message.HavePromoCodeMessage)
        }
        binding.coursePurchasePromoCodeInput.doAfterTextChanged { text: Editable? ->
            val length = text?.length ?: 0
            binding.coursePurchasePromoCodeInputDismiss.isVisible = length != 0
            binding.coursePurchasePromoCodeSubmitAction.isVisible = length != 0

            if ((promoCodeState as? PromoCodeState.Valid)?.text == text.toString()) {
                return@doAfterTextChanged
            }

            coursePurchaseViewModel.onNewMessage(CoursePurchaseFeature.Message.PromoCodeEditingMessage)
        }
        binding.coursePurchasePromoCodeInputDismiss.setOnClickListener {
            binding.coursePurchasePromoCodeInput.setText("")
        }
        binding.coursePurchasePromoCodeSubmitAction.setOnClickListener {
            coursePurchaseViewModel.onNewMessage(
                CoursePurchaseFeature.Message.PromoCodeCheckMessage(binding.coursePurchasePromoCodeInput.text.toString())
            )
        }
    }

    fun setViewVisibility(isVisible: Boolean) {
        binding.coursePromoCodeAction.isVisible = isVisible
        binding.coursePurchasePromoCodeInputContainer.isVisible = isVisible
        binding.coursePurchasePromoCodeInput.isVisible = isVisible
        binding.coursePurchasePromoCodeInputDismiss.isVisible = isVisible
        binding.coursePurchasePromoCodeSubmitAction.isVisible = isVisible
        binding.coursePurchasePromoCodeResultMessage.isVisible = isVisible
    }

    fun render(state: PromoCodeState) {
        this.promoCodeState = state
        binding.coursePromoCodeAction.isVisible = state is PromoCodeState.Idle
        binding.coursePurchasePromoCodeInputContainer.isVisible = state !is PromoCodeState.Idle
        binding.coursePurchasePromoCodeInputDismiss.isEnabled = state !is PromoCodeState.Checking
        binding.coursePurchasePromoCodeSubmitAction.isEnabled = state is PromoCodeState.Editing
        binding.coursePurchasePromoCodeInput.isEnabled = state !is PromoCodeState.Checking

        binding.coursePurchasePromoCodeResultMessage.isVisible = state is PromoCodeState.Checking || state is PromoCodeState.Valid || state is PromoCodeState.Invalid

        val (messageRes, colorRes) = getPromoCodeResultMessage(state)
        if (messageRes != -1 && colorRes != -1) {
            val context = binding.root.context
            binding.coursePurchasePromoCodeResultMessage.text = context.getString(messageRes)
            binding.coursePurchasePromoCodeResultMessage.setTextColor(AppCompatResources.getColorStateList(context, colorRes))
        }

        binding.coursePurchasePromoCodeSubmitAction.setImageDrawable(getDrawableForSubmitAction(state))
        setEditTextFromState(state)
        layerListDrawableDelegate.showLayer(getBackgroundLayer(state))
    }

    private fun getPromoCodeResultMessage(promoCodeState: PromoCodeState): Pair<Int, Int> =
        when (promoCodeState) {
            is PromoCodeState.Idle, is PromoCodeState.Editing ->
                -1 to -1
            is PromoCodeState.Checking ->
                R.string.course_purchase_promocode_checking to R.color.color_overlay_violet
            is PromoCodeState.Valid ->
                R.string.course_purchase_promocode_valid to R.color.color_overlay_green
            is PromoCodeState.Invalid ->
                R.string.course_purchase_promocode_invalid to R.color.color_overlay_red
        }

    private fun setEditTextFromState(state: PromoCodeState) {
        when (state) {
            is PromoCodeState.Checking -> {
                binding.coursePurchasePromoCodeInput.setTextIfChanged(state.text)
            }
            is PromoCodeState.Valid ->
                binding.coursePurchasePromoCodeInput.setTextIfChanged(state.text)
            else ->
                return
        }
    }

    private fun getDrawableForSubmitAction(state: PromoCodeState): Drawable? =
        when (state) {
            is PromoCodeState.Idle, is PromoCodeState.Editing ->
                AppCompatResources.getDrawable(binding.root.context, R.drawable.ic_arrow_forward)
            is PromoCodeState.Checking -> {
                val evaluationDrawable = AnimationDrawable()
                val context = binding.root.context
                evaluationDrawable.addFrame(context.getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_1), EVALUATION_FRAME_DURATION_MS)
                evaluationDrawable.addFrame(context.getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_2), EVALUATION_FRAME_DURATION_MS)
                evaluationDrawable.addFrame(context.getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_3), EVALUATION_FRAME_DURATION_MS)
                evaluationDrawable.isOneShot = false
                evaluationDrawable.start()
                evaluationDrawable
            }
            is PromoCodeState.Invalid ->
                AppCompatResources.getDrawable(binding.root.context, R.drawable.ic_step_quiz_wrong)
            is PromoCodeState.Valid ->
                AppCompatResources.getDrawable(binding.root.context, R.drawable.ic_step_quiz_correct)
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