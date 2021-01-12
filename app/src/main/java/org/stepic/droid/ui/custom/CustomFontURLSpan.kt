package org.stepic.droid.ui.custom

import android.text.TextPaint
import org.stepik.android.domain.base.InternalDeeplinkURLSpan

class CustomFontURLSpan(url: String) : InternalDeeplinkURLSpan(url) {
    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        ds.isUnderlineText = false
    }
}