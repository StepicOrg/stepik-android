package org.stepic.droid.adaptive.ui.custom.morphing

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.animation.Interpolator

class MorphingAnimation(
    private val view: MorphingView,
    private val to: MorphingView.MorphParams,
    val interpolator: Interpolator? = null
) {
    val set = AnimatorSet()

    fun initSet() {
        val from = view.getMorphParams()

        val fromColor = if (from.backgroundColor == -1) 0x0 else from.backgroundColor
        val toColor = if (to.backgroundColor == -1) fromColor else to.backgroundColor

        val colorAnimator = ObjectAnimator.ofInt(view.drawableWrapper, "color", fromColor, toColor)
        colorAnimator.setEvaluator(ArgbEvaluator())

        val morphParamsAnimator = ValueAnimator.ofFloat(0f, 1f)
        morphParamsAnimator.addUpdateListener {
            val scale = it.animatedValue as Float

            view.morph(MorphingView.MorphParams(
                    getScaledValue(scale, from.cornerRadius, to.cornerRadius),
                    -1,

                    getScaledValue(scale, from.width.toFloat(), to.width.toFloat()).toInt(),
                    getScaledValue(scale, from.height.toFloat(), to.height.toFloat()).toInt(),

                    getScaledValue(scale, from.marginLeft.toFloat(), to.marginLeft.toFloat()).toInt(),
                    getScaledValue(scale, from.marginTop.toFloat(), to.marginTop.toFloat()).toInt(),
                    getScaledValue(scale, from.marginRight.toFloat(), to.marginRight.toFloat()).toInt(),
                    getScaledValue(scale, from.marginBottom.toFloat(), to.marginBottom.toFloat()).toInt(),

                    if (from.text == to.text || to.text == null) from.text else "",
                    getScaledValue(scale, from.textSize, to.textSize)
            ))
        }

        interpolator?.let { set.interpolator = it }
        set.playTogether(colorAnimator, morphParamsAnimator)

        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                view.morph(to)
                runnable?.run()
                next?.start()
            }
        })
    }

    fun setDuration(duration: Long): MorphingAnimation {
        set.duration = duration
        return this
    }

    private fun getScaledValue(scale: Float, from: Float, to: Float, not: Float = -1f) =
            if (to != not) {
                from + (to - from) * scale
            } else {
                to
            }


    private var next: MorphingAnimation? = null
    private var runnable: Runnable? = null

    fun withEndAction(run: Runnable): MorphingAnimation {
        runnable = run
        return this
    }

    private var startDelay = 0L

    fun setStartDelay(delay: Long): MorphingAnimation {
        startDelay = delay
        return this
    }

    fun getAnimator(): Animator {
        initSet()
        set.startDelay = startDelay
        return set
    }

    fun start(): MorphingAnimation {
        getAnimator().start()
        return this
    }

    fun chain(next: MorphingAnimation): MorphingAnimation {
        this.next = next
        return this
    }
}