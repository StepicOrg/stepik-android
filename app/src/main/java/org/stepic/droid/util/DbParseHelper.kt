package org.stepic.droid.util

object DbParseHelper {

    private const val DEFAULT_SEPARATOR = "__,__"

    @JvmStatic
    @JvmOverloads
    fun parseStringToLongArray(str: String?, separator: String = DEFAULT_SEPARATOR): LongArray? =
            parseStringToLongList(str, separator)?.toLongArray()

    @JvmStatic
    @JvmOverloads
    fun parseStringToLongList(str: String?, separator: String = DEFAULT_SEPARATOR): List<Long>? =
            try {
                str?.split(separator)?.map { it.trim().toLong() }
            } catch (e: Exception) {
                null
            }

    @JvmStatic
    @JvmOverloads
    fun parseLongArrayToString(longArray: LongArray?, separator: String = DEFAULT_SEPARATOR): String? {
        if (longArray == null || longArray.isEmpty()) return null

        return longArray.joinToString(separator)
    }

    @JvmStatic
    @JvmOverloads
    fun parseLongListToString(longArray: List<Long>?, separator: String = DEFAULT_SEPARATOR): String? {
        if (longArray == null || longArray.isEmpty()) return null

        return longArray.joinToString(separator)
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


