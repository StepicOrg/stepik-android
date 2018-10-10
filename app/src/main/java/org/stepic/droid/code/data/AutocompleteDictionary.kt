package org.stepic.droid.code.data

import org.stepic.droid.util.isNotOrdered

/**
 * Class for fast search strings with common prefix
 */
class AutocompleteDictionary(private val dict: Array<String>, needSort: Boolean = true, val isCaseSensitive : Boolean = true ) {
    companion object {
        /**
         * Returns next in chars string.
         * e.g.: incrementString("aa") == "ab"
         */
        fun incrementString(string: String): String {
            val chars = string.toCharArray()
            for (i in chars.size - 1 downTo 0) {
                if (chars[i] + 1 < chars[i]) {
                    chars[i] = 0.toChar()
                } else {
                    chars[i] = chars[i] + 1
                    break
                }
            }
            return if (chars.isEmpty() || chars[0] == 0.toChar()) {
                1.toChar() + String(chars)
            } else {
                String(chars)
            }
        }

        /**
         * Kotlin's binarySearch provides element position or position where element should be multiplied by -1.
         * This method convert result in second case to positive position.
         */
        private fun getBinarySearchPosition(pos: Int) =
                if (pos < 0)
                    -(pos + 1)
                else
                    pos
    }

    init {
        if (needSort) {
            dict.sort()
        } else {
            if (dict.isNotOrdered()) throw IllegalArgumentException("Given array should be sorted")
        }
    }

    fun getAutocompleteForPrefix(prefix: String): List<String> {
        val start = dict.binarySearch(prefix).let { getBinarySearchPosition(it) }
        val end = dict.binarySearch(incrementString(prefix)).let { getBinarySearchPosition(it) }
        return dict.slice(start until end)
    }
}