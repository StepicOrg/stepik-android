package org.stepik.android.cache.calendar

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.os.Build
import android.provider.CalendarContract
import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.util.AppConstants
import org.stepik.android.data.calendar.source.CalendarCacheDataSource
import org.stepik.android.domain.calendar.model.CalendarEventData
import org.stepik.android.domain.calendar.model.CalendarItem
import java.util.*
import javax.inject.Inject

class CalendarCacheDataSourceImpl
@Inject
constructor(
    private val contentResolver: ContentResolver
) : CalendarCacheDataSource {

    override fun syncCalendarEventData(calendarEventData: CalendarEventData, calendarItem: CalendarItem): Single<Long> =
        insertCalendarEventData(calendarEventData, calendarItem)

    override fun getCalendarPrimaryItems(): Single<List<CalendarItem>> =
        Single.create<List<CalendarItem>> { emitter ->
            val listOfCalendarItems = mutableListOf<CalendarItem>()
            val cursor: Cursor? = contentResolver.query(CalendarContract.Calendars.CONTENT_URI, null, null, null, null)
            try {
                if (cursor != null) {
                    cursor.moveToFirst()
                    while (!cursor.isAfterLast && !emitter.isDisposed) {
                        val indexId = cursor.getColumnIndex(CalendarContract.Calendars._ID)
                        var indexIsPrimary = -1
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                indexIsPrimary = cursor.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)
                            }
                            if (indexIsPrimary < 0) {
                                indexIsPrimary = cursor.getColumnIndex("COALESCE(isPrimary, ownerAccount = account_name)")//look at http://stackoverflow.com/questions/25870556/check-if-calendar-is-primary
                            }
                        } catch (ex: NoSuchFieldError) {
                            //if no such field we will show all calendars, see below
                        }
                        val indexOwner = cursor.getColumnIndex(CalendarContract.Calendars.OWNER_ACCOUNT)

                        var isPrimary = false
                        if (indexIsPrimary >= 0) {
                            isPrimary = cursor.getInt(indexIsPrimary) > 0
                        }
                        val calendarId = cursor.getLong(indexId)
                        val owner = cursor.getString(indexOwner)

                        if (isPrimary) {
                            listOfCalendarItems.add(CalendarItem(calendarId, owner, isPrimary))
                        }

                        cursor.moveToNext()
                    }
                }
                if (!emitter.isDisposed) {
                    emitter.onSuccess(listOfCalendarItems)
                }
            } catch (e: Exception) {
                if (!emitter.isDisposed) {
                    emitter.onError(e)
                }
            } finally {
                cursor?.close()
            }
        }

    private fun mapContentValues(calendarEventData: CalendarEventData, calendarItem: CalendarItem): ContentValues {
        val dateEndInMillis = calendarEventData.date.time
        val dateStartInMillis = dateEndInMillis - AppConstants.MILLIS_IN_1HOUR

        val contentValues = ContentValues()
        contentValues.put(CalendarContract.Events.DTSTART, dateStartInMillis)
        contentValues.put(CalendarContract.Events.DTEND, dateEndInMillis)
        contentValues.put(CalendarContract.Events.TITLE, calendarEventData.title)
        contentValues.put(CalendarContract.Events.CALENDAR_ID, calendarItem.calendarId)
        contentValues.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        contentValues.put(CalendarContract.Events.HAS_ALARM, 1)

        return contentValues
    }

    override fun deleteEventsById(ids: List<Long>): Completable =
        Completable.fromAction {
            ids.forEach{ deleteEventById(it) }
        }

    private fun insertCalendarEventData(calendarEventData: CalendarEventData, calendarItem: CalendarItem): Single<Long> =
        Single.fromCallable {
            return@fromCallable contentResolver
                    .insert(CalendarContract.Events.CONTENT_URI, mapContentValues(calendarEventData, calendarItem))
                    .lastPathSegment.toLong()
        }

    private fun deleteEventById(id: Long) =
        contentResolver.delete(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id), null, null)

}