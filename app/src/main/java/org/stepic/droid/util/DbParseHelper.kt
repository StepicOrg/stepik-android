package org.stepic.droid.util

object DbParseHelper {

    private const val DEFAULT_SEPARATOR = "__,__"
    private const val PREFIX = "["
    private const val POSTFIX = "]"

    @JvmStatic
    @JvmOverloads
    fun parseStringToLongArray(str: String?, separator: String = DEFAULT_SEPARATOR, escapeSymbols: Boolean = false): LongArray? =
            parseStringToLongList(str, separator, escapeSymbols)?.toLongArray()

    @JvmStatic
    @JvmOverloads
    fun parseStringToLongList(str: String?, separator: String = DEFAULT_SEPARATOR, escapeSymbols: Boolean = false): List<Long>? =
            try {
                str?.split(separator)?.map {
                    if (escapeSymbols) {
                        it.trim().removeSurrounding(PREFIX, POSTFIX).toLong()
                    } else {
                        it.trim().toLong()
                    }
                }
            } catch (e: Exception) {
                null
            }

    @JvmStatic
    @JvmOverloads
    fun parseLongArrayToString(longArray: LongArray?, separator: String = DEFAULT_SEPARATOR, escapeSymbols: Boolean = false): String? {
        if (longArray == null || longArray.isEmpty()) return null

        return if (escapeSymbols) {
            longArray.joinToString(separator, transform = { "$PREFIX$it$POSTFIX" } )
        } else {
            longArray.joinToString(separator)
        }
    }

    @JvmStatic
    @JvmOverloads
    fun parseLongListToString(longArray: List<Long>?, separator: String = DEFAULT_SEPARATOR, escapeSymbols: Boolean = false): String? {
        if (longArray == null || longArray.isEmpty()) return null

        return if (escapeSymbols) {
            longArray.joinToString(separator, transform = { "$PREFIX$it$POSTFIX" })
        } else {
            longArray.joinToString(separator)
        }
    }

    @JvmStatic
    @JvmOverloads
    fun parseStringArrayToString(array: Array<String>?, separator: String = DEFAULT_SEPARATOR): String? {
        if (array == null || array.isEmpty()) return null

        return array.joinToString(separator)
    }

    @JvmStatic
    @JvmOverloads
    fun parseStringToStringList(str: String?, separator: String = DEFAULT_SEPARATOR): List<String>? =
            str?.split(separator)?.map(String::trim)


    @JvmStatic
    @JvmOverloads
    fun parseStringToStringArray(str: String?, separator: String = DEFAULT_SEPARATOR): Array<String>? =
            parseStringToStringList(str, separator)?.toTypedArray()

}


