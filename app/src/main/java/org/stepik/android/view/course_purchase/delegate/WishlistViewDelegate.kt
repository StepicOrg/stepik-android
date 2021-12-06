package org.stepik.android.view.course_purchase.delegate

import android.graphics.drawable.AnimationDrawable
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import org.stepic.droid.R
import org.stepik.android.presentation.course_purchase.CoursePurchaseFeature.WishlistState
import ru.nobird.android.view.base.ui.extension.getDrawableCompat

class WishlistViewDelegate(
    private val wishlistButton: MaterialButton
) {
    companion object {
        private const val EVALUATION_FRAME_DURATION_MS = 250
    }

    private val context = wishlistButton.context

    fun setViewVisibility(isVisible: Boolean) {
        wishlistButton.isVisible = isVisible
    }

    fun render(state: WishlistState) {
        val messageResId =
            when (state) {
                WishlistState.Idle ->
                    R.string.course_purchase_wishlist_add
                WishlistState.Adding ->
                    R.string.course_purchase_wishlist_adding
                WishlistState.Wishlisted ->
                    R.string.course_purchase_wishlist_added
            }
        wishlistButton.isEnabled = state is WishlistState.Idle
        wishlistButton.setText(messageResId)
        resolveButtonDrawable(state)
    }

    private fun resolveButtonDrawable(state: WishlistState) {
        if (state is WishlistState.Adding) {
            val evaluationDrawable = AnimationDrawable()
            evaluationDrawable.addFrame(context.getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_1), EVALUATION_FRAME_DURATION_MS)
            evaluationDrawable.addFrame(context.getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_2), EVALUATION_FRAME_DURATION_MS)
            evaluationDrawable.addFrame(context.getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_3), EVALUATION_FRAME_DURATION_MS)
            evaluationDrawable.isOneShot = false

            wishlistButton.setCompoundDrawablesWithIntrinsicBounds(evaluationDrawable, null, null, null)
            evaluationDrawable.start()
        } else {
            wishlistButton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
        }
    }
}