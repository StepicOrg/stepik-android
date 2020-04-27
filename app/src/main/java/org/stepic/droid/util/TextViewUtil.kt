package org.stepic.droid.util

import android.text.SpannableString
import android.text.TextPaint
import android.text.style.URLSpan
import android.widget.TextView


fun stripUnderlinesFromLinks(tv: TextView) {
    val spannable = SpannableString(tv.text)
    val spans = spannable.getSpans(0, spannable.length, URLSpan::class.java)

    spans.forEach {
        val start = spannable.getSpanStart(it)
        val end = spannable.getSpanEnd(it)
        val flags = spannable.getSpanFlags(it)
        spannable.removeSpan(it)
        spannable.setSpan(URLSpanWithoutUnderline(it.url), start, end, flags)
    }
    tv.text = spannable
}

private class URLSpanWithoutUnderline(url: String) : URLSpan(url) {
    override fun updateDrawState(textPaint: TextPaint) {
        super.updateDrawState(textPaint)
        textPaint.isUnderlineText = false
    }
}
