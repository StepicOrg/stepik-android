package org.stepik.android.cache.base.mapper

import androidx.room.TypeConverter

class CollectionConverter {
    companion object {
        private const val PREFIX = "["
        private const val POSTFIX = "]"
        private const val SEPARATOR = "__,__"
    }

    @TypeConverter
    fun listLongToString(list: List<Long>?): String? =
        list?.joinToString(separator = SEPARATOR, transform = ::escapeId)

    @TypeConverter
    fun stringToListLong(value: String?): List<Long>? =
        value?.split(SEPARATOR)?.mapNotNull { unescapeId(it.trim()).toLongOrNull() }

    private fun escapeId(value: Long): String =
        "$PREFIX$value$POSTFIX"

    private fun unescapeId(value: String): String =
        value.removeSurrounding(PREFIX, POSTFIX)
}