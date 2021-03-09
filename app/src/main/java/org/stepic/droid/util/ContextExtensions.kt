package org.stepic.droid.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
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

fun Context.copyTextToClipboard(label: String? = null, textToCopy: String, toastMessage: String) {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText(label, textToCopy))
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
 * "com.google.android.googlequicksearchbox" is necessary to launch Scene Viewer
 * Reference: https://developers.google.com/ar/develop/java/scene-viewer#3d-or-ar
 */

fun Context.isARSupported(): Boolean =
    try {
        packageManager.getApplicationInfo("com.google.android.googlequicksearchbox", 0)
        true
    } catch (e : PackageManager.NameNotFoundException) {
        false
    }