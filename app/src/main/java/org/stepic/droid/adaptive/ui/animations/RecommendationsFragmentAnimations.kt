package org.stepic.droid.adaptive.ui.animations

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.github.jinatonic.confetti.CommonConfetti
import org.stepic.droid.R
import org.stepic.droid.adaptive.ui.custom.morphing.MorphingHelper
import org.stepic.droid.adaptive.ui.custom.morphing.MorphingView
import org.stepic.droid.di.AppSingleton
import javax.inject.Inject

@AppSingleton
class RecommendationsFragmentAnimations
@Inject
constructor(
        context: Context
){
    companion object {
        private const val ANIMATION_START_DELAY_FOR_VIEWS_MS = 1500L
        private const val ANIMATION_DURATION_MS = 200L
        private const val FAST_ANIMATION_DURATION_MS = 100L
    }

    private val confettiColors: IntArray = intArrayOf(
            Color.BLACK,
            ContextCompat.getColor(context, R.color.black1_alpha_19),
            ContextCompat.getColor(context, R.color.adaptive_color_correct)
    )

    fun playStreakBubbleAnimation(greenStreakBubble: View) {
        greenStreakBubble.alpha = 1f
        greenStreakBubble.animate()
                .alpha(0f)
                .setInterpolator(DecelerateInterpolator())
                .setStartDelay(ANIMATION_START_DELAY_FOR_VIEWS_MS)
                .setDuration(ANIMATION_DURATION_MS)
                .start()
    }

    fun playStreakSuccessAnimationSequence(
            root: CoordinatorLayout,
            streakSuccessContainer: MorphingView,
            expProgress: View,
            expInc: TextView,
            expBubble: View) {
        SupportViewPropertyAnimator(streakSuccessContainer)
                .alpha(1f)
                .setStartDelay(0)
                .setDuration(ANIMATION_DURATION_MS)
                .withEndAction (Runnable { playStreakMorphAnimation(root, streakSuccessContainer, expProgress, expInc, expBubble) })
                .start()
    }

    private fun playStreakMorphAnimation(
            root: CoordinatorLayout,
            streakSuccessContainer: MorphingView,
            expProgress: View,
            expInc: TextView,
            expBubble: View) {
        val params = streakSuccessContainer.initialMorphParams

        MorphingHelper.morphStreakHeaderToIncBubble(streakSuccessContainer, expInc)
                .setStartDelay(ANIMATION_START_DELAY_FOR_VIEWS_MS)
                .withEndAction(Runnable {
                    expProgress.visibility = View.VISIBLE
                    startConfettiExplosion(root, expBubble)

                    SupportViewPropertyAnimator(streakSuccessContainer)
                            .alpha(0f)
                            .setInterpolator(DecelerateInterpolator())
                            .setStartDelay(ANIMATION_START_DELAY_FOR_VIEWS_MS)
                            .setDuration(ANIMATION_DURATION_MS)
                            .withEndAction (Runnable { streakSuccessContainer.morph(params) })
                            .start()
                })
                .setDuration(FAST_ANIMATION_DURATION_MS)
                .start()

        expProgress.visibility = View.INVISIBLE
    }


    fun startConfettiExplosion(root: CoordinatorLayout, expBubble: View) {
        val x = (expBubble.x + (expBubble.parent as View).x).toInt() + expBubble.width / 2
        val y = (expBubble.y + expBubble.pivotY).toInt()
        CommonConfetti.explosion(root, x, y, confettiColors).oneShot()
    }

    fun playStreakFailedAnimation(streakContainer: View, expProgress: View) {
        SupportViewPropertyAnimator(streakContainer)
                .alpha(1f)
                .setStartDelay(0)
                .setDuration(ANIMATION_DURATION_MS)
                .withEndAction(Runnable {
                    SupportViewPropertyAnimator(streakContainer)
                            .setStartDelay(ANIMATION_START_DELAY_FOR_VIEWS_MS)
                            .setDuration(ANIMATION_DURATION_MS)
                            .alpha(0f)
                            .withEndAction(Runnable { expProgress.visibility = View.VISIBLE })
                            .start()
                }).start()
        expProgress.visibility = View.INVISIBLE
    }
}