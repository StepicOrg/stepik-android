package org.stepic.droid.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

const val SQL_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss"

fun Date.toSQLDatetime(): String {
    val dateFormat = SimpleDateFormat(SQL_DATETIME_PATTERN, Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    return dateFormat.format(this)
}

fun dateFromSQLDatetime(datetime: String): Date {
    val dateFormat = SimpleDateFormat(SQL_DATETIME_PATTERN, Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    return dateFormat.parse(datetime)
}