package org.stepik.android.view.base.ui.mapper

import android.content.Context
import android.support.annotation.PluralsRes
import org.stepic.droid.R

object DateMapper {
    fun mapToRelativeDate(context: Context, nowMillis: Long, thenMillis: Long): String {
        val diff = (nowMillis - thenMillis) / 1000 / 60 // minutes

        return if (diff == 0L) {
            context.getString(R.string.relative_date_now)
        } else {
            context.getString(R.string.relative_date_pattern, mapMinutesToRelativeDate(context, diff))
        }
    }

    private fun mapMinutesToRelativeDate(context: Context, diffMinutes: Long): String {
        var diff = diffMinutes

        for (dateUnit in RelativeDateUnit.values()) {
            if (diff / dateUnit.units == 0L) {
                return context.resources.getQuantityString(dateUnit.pluralRes, diff.toInt(), diff)
            } else {
                diff /= dateUnit.units
            }
        }

        return context.resources.getQuantityString(R.plurals.year, diff.toInt(), diff)
    }

    enum class RelativeDateUnit(
        val units: Long,

        @PluralsRes
        val pluralRes: Int
    ) {
        MINUTES(60, R.plurals.minutes),
        HOURS(24, R.plurals.hours),
        DAYS(7, R.plurals.day),
        WEEKS(5, R.plurals.week),
        MONTHS(12, R.plurals.month)
    }
}