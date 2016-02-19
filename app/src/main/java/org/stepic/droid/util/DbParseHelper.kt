package org.stepic.droid.util

import android.util.Log

object DbParseHelper {

    private val DELIMITER = "__,__"

    @JvmStatic
    @JvmOverloads
    fun parseStringToLongArray(str: String?, delimiter: String = DELIMITER): LongArray? {
        if (str == null) return null

        val strArray = str.split(delimiter)
        val result = LongArray(strArray.size)
        strArray.forEachIndexed { i, value -> result[i] = value.trim().toLong() }
        return result
    }

    @JvmStatic
    @JvmOverloads
    fun parseLongArrayToString(array: LongArray?, delimiter: String = DELIMITER): String? {
        if (array == null || array.size == 0) return null

        val stringBuilder = StringBuilder()
        for (i in array.indices) {
            stringBuilder.append(array[i])
            if (i != array.size - 1)
                stringBuilder.append(delimiter)
        }
        return stringBuilder.toString()
    }

    @JvmStatic
    @JvmOverloads
    fun parseStringArrayToString(array: Array<String>?, delimiter: String = DELIMITER): String? {
        if (array == null || array.size == 0) return null

        val sb = StringBuilder()
        for (i in array.indices) {
            sb.append(array[i])
            if (i != array.size - 1)
                sb.append(delimiter)
        }
        return sb.toString()
    }

    @JvmStatic
    @JvmOverloads
    fun parseStringToStringArray(str: String?, delimiter: String = DELIMITER): Array<String?>? {
        if (str == null) return null
        val strArray = str.split(delimiter)
        val result = arrayOfNulls<String>(strArray.size)
        strArray.forEachIndexed { i, el ->
            val trimmed = el.trim()
            result[i] = trimmed
        }
        return result
    }

    @JvmStatic
    fun parseLongArrayToStringArray(array: LongArray): Array<String?> {
        val result = arrayOfNulls<String>(array.size)
        for (i in array.indices) {
            result[i] = array[i].toString()
        }
        return result
    }

}


