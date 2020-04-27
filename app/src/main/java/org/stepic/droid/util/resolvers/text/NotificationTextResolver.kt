package org.stepic.droid.util.resolvers.text

import android.text.SpannableString
import android.text.style.URLSpan
import org.stepic.droid.ui.custom.CustomFontURLSpan
import javax.inject.Inject

class NotificationTextResolver
@Inject
constructor(
    private val textResolver: TextResolver
) {
    fun resolveNotificationText(content: String?): CharSequence {
        val spanned = SpannableString(textResolver.fromHtml(content))
        spanned.getSpans(0, spanned.length, URLSpan::class.java).forEach {
            val start = spanned.getSpanStart(it)
            val end = spanned.getSpanEnd(it)
            val flags = spanned.getSpanFlags(it)
            spanned.removeSpan(it)
            spanned.setSpan(CustomFontURLSpan(it.url), start, end, flags)
        }
        return spanned
    }
}