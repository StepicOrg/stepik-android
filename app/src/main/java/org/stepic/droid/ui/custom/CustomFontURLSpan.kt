package org.stepic.droid.ui.custom

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.URLSpan

class CustomFontURLSpan(url: String, private val typeface: Typeface) : URLSpan(url) {
    override fun updateDrawState(ds: TextPaint?) {
        ds?.typeface = typeface
    }
}