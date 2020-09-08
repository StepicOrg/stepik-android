package org.stepik.android.cache.base.mapper

import androidx.room.TypeConverter
import java.util.Date

class DateConverter {
    @TypeConverter
    fun dateToLong(date: Date?): Long =
        date?.time ?: -1

    @TypeConverter
    fun longToDate(long: Long): Date? =
        long.takeIf { it > -1 }?.let(::Date)
}