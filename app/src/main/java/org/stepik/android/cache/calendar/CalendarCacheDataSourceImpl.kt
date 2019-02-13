package org.stepik.android.cache.calendar

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.os.Build
import android.provider.CalendarContract
import io.reactivex.Observable
import io.reactivex.Single
import org.stepic.droid.model.CalendarItem
import org.stepik.android.data.calendar.source.CalendarCacheDataSource
import org.stepik.android.domain.calendar.model.CalendarEventData
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class CalendarCacheDataSourceImpl
@Inject
constructor(
    private val contentResolver: ContentResolver
) : CalendarCacheDataSource {

    override fun syncCalendarEventData(calendarEventData: CalendarEventData, calendarItem: CalendarItem): Observable<Long> =
        when(isEventInAnyCalendar(calendarEventData.eventId)) {
            true -> updateCalendarEventData(calendarEventData, calendarItem)
            false -> insertCalendarEventData(calendarEventData, calendarItem)
        }


    override fun getCalendarPrimaryItems(): Single<List<CalendarItem>> {
        return Single.fromCallable {
            val listOfCalendarItems = ArrayList<CalendarItem>()
            contentResolver.query(CalendarContract.Calendars.CONTENT_URI, null, null, null, null).use {
                it.moveToFirst()
                while (!it.isAfterLast) {
                    val indexId = it.getColumnIndex(CalendarContract.Calendars._ID)
                    var indexIsPrimary = -1
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            indexIsPrimary = it.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)
                        }
                        if (indexIsPrimary < 0) {
                            indexIsPrimary = it.getColumnIndex("COALESCE(isPrimary, ownerAccount = account_name)")//look at http://stackoverflow.com/questions/25870556/check-if-calendar-is-primary
                        }
                    } catch (ex: NoSuchFieldError) {
                        //if no such field we will show all calendars, see below
                    }
                    val indexOwner = it.getColumnIndex(CalendarContract.Calendars.OWNER_ACCOUNT)

                    var isPrimary = false
                    if (indexIsPrimary >= 0) {
                        isPrimary = it.getInt(indexIsPrimary) > 0
                    }
                    val calendarId = it.getLong(indexId)
                    val owner = it.getString(indexOwner)

                    if (isPrimary) {
                        listOfCalendarItems.add(CalendarItem(calendarId, owner, isPrimary))
                    }

                    it.moveToNext()
                }
            }
            listOfCalendarItems
        }
    }

    private fun mapContentValues(calendarEventData: CalendarEventData, calendarItem: CalendarItem): ContentValues {
        val dateEndInMillis = calendarEventData.deadLine.time //UTC
        val dateStartInMillis = dateEndInMillis - 3600000L

        val contentValues = ContentValues()
        contentValues.put(CalendarContract.Events.DTSTART, dateStartInMillis)
        contentValues.put(CalendarContract.Events.DTEND, dateEndInMillis)
        contentValues.put(CalendarContract.Events.TITLE, calendarEventData.title)
        contentValues.put(CalendarContract.Events.CALENDAR_ID, calendarItem.calendarId)
        contentValues.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        contentValues.put(CalendarContract.Events.HAS_ALARM, 1)

        return contentValues
    }

    private fun insertCalendarEventData(calendarEventData: CalendarEventData, calendarItem: CalendarItem): Observable<Long> {
        return Observable.fromCallable {
            val contentValues = mapContentValues(calendarEventData, calendarItem)
            val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, contentValues)
            return@fromCallable uri.lastPathSegment.toLong()
        }
    }

    private fun updateCalendarEventData(calendarEventData: CalendarEventData, calendarItem: CalendarItem): Observable<Long> =
        Observable.fromCallable {
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, calendarEventData.eventId)
            contentResolver.update(uri, mapContentValues(calendarEventData, calendarItem), null, null)
            return@fromCallable uri.lastPathSegment.toLong()
        }

    private fun isEventInAnyCalendar(eventId: Long): Boolean {
        contentResolver
                .query(CalendarContract.Events.CONTENT_URI, arrayOf(CalendarContract.Events._ID, CalendarContract.Events.CALENDAR_ID), CalendarContract.Events._ID + " = ? ", arrayOf(eventId.toString()), null)
                .use {
                    it.moveToFirst()
                    if (!it.isAfterLast) {
                        return true
                    }
                    return false
                }
    }
}