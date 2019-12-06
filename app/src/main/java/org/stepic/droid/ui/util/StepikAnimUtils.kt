package org.stepic.droid.ui.util

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation

private const val durationMillis = 300

fun expand(view: View, animationListener: Animation.AnimationListener? = null) {
    view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    val targetHeight = view.measuredHeight

    // Older versions of android (pre API 21) cancel animations for views with a height of 0.
    view.layoutParams.height = 1
    view.visibility = View.VISIBLE
    val a = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            view.layoutParams.height = if (interpolatedTime == 1f)
                ViewGroup.LayoutParams.WRAP_CONTENT
            else
                (targetHeight * interpolatedTime).toInt()
            view.requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    a.duration = durationMillis.toLong()
    animationListener?.let { a.setAnimationListener(it) }
    view.startAnimation(a)
}

fun collapse(view: View, animationListener: Animation.AnimationListener? = null) {
    val initialHeight = view.measuredHeight

    val a = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            if (interpolatedTime == 1f) {
                view.visibility = View.GONE
            } else {
                view.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                view.requestLayout()
            }
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    a.duration = durationMillis.toLong()
    animationListener?.let { a.setAnimationListener(it) }
    view.startAnimation(a)
}