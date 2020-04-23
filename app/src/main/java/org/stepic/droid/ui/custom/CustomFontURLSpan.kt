package org.stepic.droid.ui.custom

import android.text.TextPaint
import android.text.style.URLSpan

class CustomFontURLSpan(url: String) : URLSpan(url) {
    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = false
    }
}