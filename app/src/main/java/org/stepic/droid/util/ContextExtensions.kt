package org.stepic.droid.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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