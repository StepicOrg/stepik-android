package org.stepic.droid.ui.util

import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.Transformation
import android.webkit.WebView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.children
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar

fun View.setHeight(height: Int) {
    layoutParams.height = height
    layoutParams = layoutParams
}

fun ViewGroup.hideAllChildren() {
    children.forEach { it.isVisible = false }
}

fun TextView.setCompoundDrawables(
    @DrawableRes start: Int = -1,
    @DrawableRes top: Int = -1,
    @DrawableRes end: Int = -1,
    @DrawableRes bottom: Int = -1
) {
    fun TextView.getDrawableOrNull(@DrawableRes res: Int) =
        if (res != -1) AppCompatResources.getDrawable(context, res) else null

    val startDrawable = getDrawableOrNull(start)
    val topDrawable = getDrawableOrNull(top)
    val endDrawable = getDrawableOrNull(end)
    val bottomDrawable = getDrawableOrNull(bottom)
    setCompoundDrawablesWithIntrinsicBounds(startDrawable, topDrawable, endDrawable, bottomDrawable)
}

fun TextView.setTextViewBackgroundWithoutResettingPadding(@DrawableRes backgroundRes: Int) {
    setBackgroundResource(backgroundRes)
}

inline fun <T : View> T.doOnGlobalLayout(crossinline action: (view: T) -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            action(this@doOnGlobalLayout)
            viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    })
}

fun View.snackbar(@StringRes messageRes: Int, length: Int = Snackbar.LENGTH_SHORT) {
    snackbar(context.getString(messageRes), length)
}

fun View.snackbar(message: String, length: Int = Snackbar.LENGTH_SHORT) {
    Snackbar
        .make(this, message, length)
        .show()
}


private const val durationMillis = 300

fun View.expand(animationListener: Animation.AnimationListener? = null) {
    measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    val targetHeight = measuredHeight

    // Older versions of android (pre API 21) cancel animations for views with a height of 0.
    layoutParams.height = 1
    visibility = View.VISIBLE
    val a = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            layoutParams.height = if (interpolatedTime == 1f)
                ViewGroup.LayoutParams.WRAP_CONTENT
            else
                (targetHeight * interpolatedTime).toInt()
            requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    a.duration = durationMillis.toLong()
    animationListener?.let { a.setAnimationListener(it) }
    startAnimation(a)
}

fun View.collapse(animationListener: Animation.AnimationListener? = null) {
    val initialHeight = measuredHeight

    val a = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            if (interpolatedTime == 1f) {
                visibility = View.GONE
            } else {
                layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                requestLayout()
            }
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    a.duration = durationMillis.toLong()
    animationListener?.let { a.setAnimationListener(it) }
    startAnimation(a)
}

fun WebView.evaluateJavascriptCompat(code: String) {
    evaluateJavascript(code, null)
}