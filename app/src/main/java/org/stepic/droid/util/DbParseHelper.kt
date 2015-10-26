package org.stepic.droid.util

object DbParseHelper {

    private val DELIMETER = "__,__"

    fun parseStringToLongArray(str: String?): LongArray? {
        if (str == null) return null

        val strArray = str.split(DELIMETER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val result = LongArray(strArray.size)
        for (i in strArray.indices)
            result[i] = java.lang.Long.parseLong(strArray[i].trim { it <= ' ' })
        return result
    }

    fun parseLongArrayToString(array: LongArray?): String? {
        if (array == null || array.size == 0) return null

        val stringBuilder = StringBuilder()
        for (i in array.indices) {
            stringBuilder.append(array[i])
            if (i != array.size - 1)
                stringBuilder.append(DELIMETER)
        }
        return stringBuilder.toString()
    }

    fun parseStringArrayToString(array: Array<String>?): String? {
        if (array == null || array.size == 0) return null

        val sb = StringBuilder()
        for (i in array.indices) {
            sb.append(array[i])
            if (i != array.size - 1)
                sb.append(DELIMETER)
        }
        return sb.toString()
    }

    fun parseStringToStringArray(str: String?): Array<String?>? {
        if (str == null) return null

        val strArray = str.split(DELIMETER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val result = arrayOfNulls<String>(strArray.size)
        for (i in strArray.indices)
            result[i] = strArray[i].trim { it <= ' ' }
        return result
    }
}
