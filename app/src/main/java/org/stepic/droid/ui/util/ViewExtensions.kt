package org.stepic.droid.ui.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import org.stepic.droid.R
import ru.nobird.android.view.base.ui.extension.setTextColor

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