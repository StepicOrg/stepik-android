package org.stepic.droid.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.DrawableRes
import android.support.annotation.LayoutRes
import androidx.annotation.StringRes
import android.support.design.widget.Snackbar
import androidx.core.content.ContextCompat
import android.support.v7.content.res.AppCompatResources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import org.stepic.droid.R
import org.stepic.droid.util.setTextColor

fun View.changeVisibility(needShow: Boolean) {
    if (needShow) {
        this.visibility = View.VISIBLE
    } else {
        this.visibility = View.GONE
    }
}

fun View.setHeight(height: Int) {
    layoutParams.height = height
    layoutParams = layoutParams
}

fun ViewGroup.children(): Iterable<View> =
    Iterable {
        object : Iterator<View> {
            private var position = 0

            override fun hasNext(): Boolean =
                position < this@children.childCount

            override fun next(): View =
                this@children.getChildAt(position++)
        }
    }

fun ViewGroup.hideAllChildren() {
    for (i in 0 until childCount) {
        getChildAt(i).changeVisibility(false)
    }
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
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        val paddingLeft = this.paddingLeft
        val paddingTop = this.paddingTop
        val paddingRight = this.paddingRight
        val paddingBottom = this.paddingBottom
        val compoundDrawablePadding = this.compoundDrawablePadding

        setBackgroundResource(backgroundRes)
        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
        this.compoundDrawablePadding = compoundDrawablePadding
    } else {
        setBackgroundResource(backgroundRes)
    }
}


fun Drawable.toBitmap(width: Int = intrinsicWidth, height: Int = intrinsicHeight): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

fun ViewGroup.inflate(@LayoutRes resId: Int, attachToRoot: Boolean = false): View =
    LayoutInflater.from(this.context).inflate(resId, this, attachToRoot)

/**
 * Performs the given action when the view tree is about to be drawn.
 */
inline fun <T : View> T.doOnPreDraw(crossinline action: (view: T) -> Unit) {
    val vto = viewTreeObserver
    vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            action(this@doOnPreDraw)
            when {
                vto.isAlive -> vto.removeOnPreDrawListener(this)
                else -> viewTreeObserver.removeOnPreDrawListener(this)
            }
            return true
        }
    })
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
        .setTextColor(ContextCompat.getColor(context, R.color.white))
        .show()
}