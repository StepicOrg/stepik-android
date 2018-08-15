package org.stepic.droid.util

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

    @JvmStatic
    fun formatBytes(bytes: Long): String {
        val unit = 1024
        if (bytes < unit) return "$bytes B"
        val exp = log(bytes.toFloat(), unit.toFloat()).toInt()
        val letter = "kMGTPE"[exp - 1]
        return "%.1f %sB".format(bytes / unit.toDouble().pow(exp), letter)
    }
}
