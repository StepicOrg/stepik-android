package org.stepic.droid.util

import androidx.core.text.HtmlCompat
import androidx.core.text.util.LinkifyCompat
import android.text.Html
import android.text.SpannableString
import android.text.util.Linkify
import kotlin.math.log
import kotlin.math.pow

object TextUtil {
    fun getIndexOfFirstSpace(text: CharSequence): Int = getIndexOfNWhitespace(text, 1)

    /**
     * @return index of N whitespace or length of line
     */
    private fun getIndexOfNWhitespace(text: CharSequence, whitespaceCount: Int): Int {
        if (whitespaceCount <= 0) {
            throw IllegalArgumentException("whitespaceCount should be positive")
        }
        var numberOfWhitespaces = 0
        text.forEachIndexed { index, c ->
            if (!Character.isWhitespace(c)) {
                return@forEachIndexed // continue
            }
            numberOfWhitespaces++
            if (numberOfWhitespaces >= whitespaceCount) {
                return index
            }
        }
        return text.length
    }

    /**
     * Format [bytes] to human readable format. If [bytes] < [smallestUnit] returns bytes in form "< smallestUnit".
     */
    @JvmStatic
    fun formatBytes(bytes: Long, smallestUnit: Long = 0): String {
        val unit = 1024

        if (smallestUnit > 0 && bytes < smallestUnit) {
            return "<" + formatBytes(bytes = smallestUnit)
        }

        if (bytes < unit) return "$bytes B"
        val exp = log(bytes.toFloat(), unit.toFloat()).toInt()
        val letter = "kMGTPE"[exp - 1]
        return "%.1f %sB".format(bytes / unit.toDouble().pow(exp), letter)
    }

    @JvmStatic
    fun linkify(text: String): String =
        SpannableString(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT))
            .let {
                LinkifyCompat.addLinks(it, Linkify.WEB_URLS)
                HtmlCompat.toHtml(it, HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
            }
}
