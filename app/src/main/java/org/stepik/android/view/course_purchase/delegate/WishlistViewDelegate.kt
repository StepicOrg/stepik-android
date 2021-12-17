package org.stepik.android.view.course_purchase.delegate

import android.graphics.drawable.AnimationDrawable
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import org.stepic.droid.R
import org.stepik.android.presentation.wishlist.WishlistOperationFeature
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

    fun render(state: WishlistOperationFeature.State, mustEnable: Boolean) {
        val messageResId =
            when (state) {
                WishlistOperationFeature.State.Idle ->
                    R.string.course_purchase_wishlist_add
                WishlistOperationFeature.State.Adding ->
                    R.string.course_purchase_wishlist_adding
                WishlistOperationFeature.State.Wishlisted ->
                    R.string.course_purchase_wishlist_added
            }

        val isButtonEnabled = state is WishlistOperationFeature.State.Idle && mustEnable

        wishlistButton.isChecked = isButtonEnabled
        wishlistButton.isEnabled = isButtonEnabled
        wishlistButton.setText(messageResId)
        resolveButtonDrawable(state)
    }

    private fun resolveButtonDrawable(state: WishlistOperationFeature.State) {
        if (state is WishlistOperationFeature.State.Adding) {
            val evaluationDrawable = AnimationDrawable()
            evaluationDrawable.addFrame(context.getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_1), EVALUATION_FRAME_DURATION_MS)
            evaluationDrawable.addFrame(context.getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_2), EVALUATION_FRAME_DURATION_MS)
            evaluationDrawable.addFrame(context.getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_3), EVALUATION_FRAME_DURATION_MS)
            evaluationDrawable.isOneShot = false

            wishlistButton.icon = evaluationDrawable
            evaluationDrawable.start()
        } else {
            wishlistButton.icon = null
        }
    }
}