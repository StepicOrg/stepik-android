package org.stepic.droid.core

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.support.annotation.WorkerThread
import android.support.v4.content.ContextCompat
import android.util.Log
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.configuration.IConfig
import org.stepic.droid.model.CalendarSection
import org.stepic.droid.model.Section
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.StringUtil
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Singleton

@Singleton
class CalendarPresenterImpl(val config: IConfig,
                            val mainHandler: IMainHandler,
                            val context: Context,
                            val threadPool: ThreadPoolExecutor,
                            val database: DatabaseFacade) : CalendarPresenter {

    private var view: CalendarExportableView? = null

    override fun checkToShowCalendar(sectionList: List<Section>?) {
        if (sectionList == null || sectionList.isEmpty()) {
            view?.onShouldBeShownCalendar(false)
            return
        }

        threadPool.execute {
            val now: Long = DateTime.now(DateTimeZone.getDefault()).millis
            val nowMinus1Hour = now - AppConstants.MILLIS_IN_1HOUR

            val ids = sectionList
                    .map { it.id }
                    .toLongArray()

            val addedCalendarSectionsMap = database.getCalendarSectionsByIds(ids)

            sectionList.forEach {
                val calendarSection: CalendarSection? = addedCalendarSectionsMap[it.id]
                // We can't check calendar permission, when we want to show widget
                if (calendarSection == null) {
                    if (isDateGreaterThanOther(it.soft_deadline, nowMinus1Hour)
                            || isDateGreaterThanOther(it.hard_deadline, nowMinus1Hour)) {
                        mainHandler.post {
                            view?.onShouldBeShownCalendar(true)
                        }
                        return@execute
                    }
                } else {
                    // we already exported in calendar this section! Check if new date in future and greater that calendar date + 30 days
                    val calendarDeadlineMillisPlusMonth = DateTime(calendarSection.mostLastDeadline).millis + AppConstants.MILLIS_IN_1MONTH
                    if ((isDateGreaterThanOther(it.soft_deadline, calendarDeadlineMillisPlusMonth) || isDateGreaterThanOther(it.hard_deadline, calendarDeadlineMillisPlusMonth))
                            && (isDateGreaterThanOther(it.soft_deadline, nowMinus1Hour) || isDateGreaterThanOther(it.hard_deadline, nowMinus1Hour))) {
                        mainHandler.post {
                            view?.onShouldBeShownCalendar(true)
                        }
                        return@execute
                    }
                }
            }

            mainHandler.post {
                view?.onShouldBeShownCalendar(false)
            }
        }
    }

    private fun isDateGreaterThanOther(deadline: String?, otherDate: Long): Boolean {
        if (deadline != null) {
            val deadlineDateTime = DateTime(deadline)
            val deadlineMillis = deadlineDateTime.millis
            if (deadlineMillis - otherDate > 0) {
                return true
            } else {
                return false
            }
        } else {
            return false
        }
    }

    override fun addDeadlinesToCalendar(sectionList: List<Section>) {
        val permissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_CALENDAR)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            view?.permissionNotGranted()
            return
        }


        val now: Long = DateTime.now(DateTimeZone.getDefault()).millis
        val nowMinus1Hour = now - AppConstants.MILLIS_IN_1HOUR
        threadPool.execute {

            val ids = sectionList
                    .map { it.id }
                    .toLongArray()

            val addedCalendarSectionsMap = database.getCalendarSectionsByIds(ids)

            sectionList.filterNotNull().forEach {
                // We can choose soft_deadline or last_deadline of the Course, if section doesn't have it, but it will pollute calendar.

                // Add upcoming deadlines of sections:
                if (isDateGreaterThanOther(it.soft_deadline, nowMinus1Hour)) {
                    val deadline = it.soft_deadline
                    if (deadline != null) {
                        addDeadlineEvent(it, deadline, DeadlineType.softDeadline, addedCalendarSectionsMap[it.id])
                    }
                }

                if (isDateGreaterThanOther(it.hard_deadline, nowMinus1Hour)) {
                    val deadline = it.hard_deadline
                    if (deadline != null) {
                        addDeadlineEvent(it, deadline, DeadlineType.hardDeadline, addedCalendarSectionsMap[it.id])
                    }
                }
            }

            mainHandler.post {
                view?.successExported()
            }
        }
    }

    @WorkerThread
    private fun addDeadlineEvent(section: Section, deadline: String, deadlineType: DeadlineType, calendarSection: CalendarSection?) {

        //FIXME: change to calendar chooser
        //
        var cursor: Cursor? = null
        val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.ACCOUNT_NAME)
        val cr = context.getContentResolver()
        cursor = cr.query(Uri.parse("content://com.android.calendar/calendars"), projection, null, null, null)
        cursor!!.moveToFirst()
        val calIds = IntArray(cursor!!.getCount())
        val calNames = arrayOfNulls<String>(cursor!!.getCount())
        for (i in calNames.indices) {
            calIds[i] = cursor!!.getInt(0)
            calNames[i] = cursor!!.getString(1)
            cursor!!.moveToNext()
        }
        //^^^^^^^^^

        val dateEndInMillis = DateTime(deadline).millis
        val dateStartInMillis = dateEndInMillis - AppConstants.MILLIS_IN_1HOUR

        val contentValues = ContentValues()
        contentValues.put(CalendarContract.Events.DTSTART, dateStartInMillis)
        contentValues.put(CalendarContract.Events.DTEND, dateEndInMillis)

        val calendarTitle = section.title + " — " + context.getString(deadlineType.deadlineTitle)
        contentValues.put(CalendarContract.Events.TITLE, calendarTitle);
        contentValues.put(CalendarContract.Events.DESCRIPTION, StringUtil.getAbsoluteUriForSection(config, section));
        contentValues.put(CalendarContract.Events.CALENDAR_ID, 1)
        contentValues.put(CalendarContract.Events.EVENT_TIMEZONE, DateTimeZone.getDefault().id)
        contentValues.put(CalendarContract.Events.HAS_ALARM, 1)

        if (calendarSection != null) {
            //FIXME: CHECK IF EVENT IS EXIST, otherwise -> it won't update
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, calendarSection.eventId)
            val rowsUpdated = context.contentResolver.update(uri, contentValues, null, null)
            Log.d("eee", "rows updated " + rowsUpdated)
            addToDatabase(section, deadlineType, deadline, calendarSection.eventId)
        } else {
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, contentValues)

            val eventId: Long = (uri.lastPathSegment).toLong()
            Log.d("eee", "eventId " + eventId)

            addToDatabase(section, deadlineType, deadline, eventId)

            val reminderValues = ContentValues()
            reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventId)
            reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_DEFAULT)
            reminderValues.put(CalendarContract.Reminders.MINUTES, AppConstants.TWO_DAY_IN_MINUTES)
            val uriReminder = context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
        }
    }

    fun addToDatabase(section: Section, deadlineType: DeadlineType, deadline: String, eventId: Long) {
        if (deadlineType == DeadlineType.hardDeadline) {
            database.addCalendarEvent(CalendarSection(section.id, eventId, deadline))
        } else if (deadlineType == DeadlineType.softDeadline) {
            val calendarSectionFromDatabase = database.getCalendarEvent(sectionId = section.id)
            if (calendarSectionFromDatabase != null) {
                val deadlineInCalendarInMillis = DateTime(calendarSectionFromDatabase.mostLastDeadline).millis
                val dateEndInMillis = DateTime(deadline).millis
                val isNeedUpdateDeadlineInDb = (dateEndInMillis - deadlineInCalendarInMillis) > 0
                if (isNeedUpdateDeadlineInDb) {
                    database.addCalendarEvent(CalendarSection(section.id, eventId, deadline))
                }
            } else {
                database.addCalendarEvent(CalendarSection(section.id, eventId, deadline))
            }
        }
    }

    override fun onStart(view: CalendarExportableView) {
        this.view = view
    }

    override fun onStop() {
        view = null
    }
}