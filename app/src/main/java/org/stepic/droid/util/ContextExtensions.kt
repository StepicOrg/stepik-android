package org.stepic.droid.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
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

fun Context.resolveAttribute(@AttrRes attributeResId: Int): TypedValue? {
    val typedValue = TypedValue()
    return if (theme.resolveAttribute(attributeResId, typedValue, true)) {
        typedValue
    } else {
        null
    }
}

@ColorInt
fun Context.resolveColorAttribute(@AttrRes attributeResId: Int): Int =
    resolveAttribute(attributeResId)?.data ?: 0

fun Context.resolveFloatAttribute(@AttrRes attributeResId: Int): Float =
    resolveAttribute(attributeResId)?.float ?: 0f

fun Context.resolveResourceIdAttribute(@AttrRes attributeResId: Int): Int =
    resolveAttribute(attributeResId)?.resourceId ?: 0

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


/**
 * True if MODE_NIGHT enabled
 */
fun Context.isNightModeEnabled(): Boolean =
    (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES