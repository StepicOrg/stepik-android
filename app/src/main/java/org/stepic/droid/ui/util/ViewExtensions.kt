package org.stepic.droid.ui.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.DrawableRes
import android.support.annotation.LayoutRes
import android.support.v7.content.res.AppCompatResources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView


fun ViewTreeObserver.removeGlobalLayoutListener(listener: ViewTreeObserver.OnGlobalLayoutListener) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
        removeOnGlobalLayoutListener(listener)
    } else {
        @Suppress("DEPRECATION") //use only on old API
        removeGlobalOnLayoutListener(listener)
    }
}

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
inline fun View.doOnPreDraw(crossinline action: (view: View) -> Unit) {
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