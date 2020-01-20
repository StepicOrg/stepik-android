package org.stepic.droid.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources

/**
 * Returns drawable for [drawableRes]
 */
fun Context.getDrawableCompat(@DrawableRes drawableRes: Int): Drawable =
    AppCompatResources.getDrawable(this, drawableRes) as Drawable

fun Context.copyTextToClipboard(label: String? = null, textToCopy: String, toastMessage: String) {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.primaryClip = ClipData.newPlainText(label, textToCopy)
    Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
}

/**
 * Workaround appcompat-1.1.0 bug https://issuetracker.google.com/issues/141132133
 */
fun Context.contextForWebView(): Context =
    if (Build.VERSION.SDK_INT in 21..22) {
        applicationContext
    } else {
        this
    }

/**
 * Converts current value in px to dp
 */
fun Int.toDp(): Int =
    this.toFloat().toDp().toInt()

/**
 * Converts current value in dp to px
 */
fun Int.toPx(): Int =
    this.toFloat().toPx().toInt()


/**
 * Converts current value in px to sp
 */
fun Int.toSp(): Int =
    this.toFloat().toSp().toInt()

/**
 * Converts current value in px to dp
 */
fun Float.toDp(): Float =
    this / Resources.getSystem().displayMetrics.density

/**
 * Converts current value in dp to px
 */
fun Float.toPx(): Float =
    this * Resources.getSystem().displayMetrics.density

/**
 * Converts current value in px to dp
 */
fun Float.toSp(): Float =
    this / Resources.getSystem().displayMetrics.scaledDensity