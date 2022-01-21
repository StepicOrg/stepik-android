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

    @TypeConverter
    fun dateListToLongList(dates: List<Date>?): List<Long>? =
        dates?.map { it.time }

    @TypeConverter
    fun longListToDateList(longs: List<Long>?): List<Date>? =
        longs?.map { Date(it) }
}