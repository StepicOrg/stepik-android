package org.stepic.droid.core

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.support.annotation.WorkerThread
import android.support.v4.content.ContextCompat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.configuration.IConfig
import org.stepic.droid.model.CalendarSection
import org.stepic.droid.model.Section
import org.stepic.droid.preferences.UserPreferences
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
                            val database: DatabaseFacade,
                            val userPrefs: UserPreferences) : CalendarPresenter {

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
            var isShownInMenu = false
            sectionList.forEach {
                val calendarSection: CalendarSection? = addedCalendarSectionsMap[it.id]
                // We can't check calendar permission, when we want to show widget
                val isDeadlineGreaterThanNow = isDateGreaterThanOther(it.soft_deadline, nowMinus1Hour) || isDateGreaterThanOther(it.hard_deadline, nowMinus1Hour)

                if (calendarSection == null) {
                    if (isDeadlineGreaterThanNow) {
                        if (userPrefs.isNeedToShowCalendarWidget) {
                            mainHandler.post {
                                view?.onShouldBeShownCalendar(true)
                            }
                        }
                        if (!isShownInMenu) {
                            isShownInMenu = true
                            mainHandler.post {
                                view?.onShouldBeShownCalendarInMenu()
                            }
                        }
                        return@execute
                    }
                } else {
                    // we already exported in calendar this section! Check if new date in future and greater that calendar date + 30 days
                    val calendarDeadlineMillisPlusMonth = DateTime(calendarSection.mostLastDeadline).millis + AppConstants.MILLIS_IN_1MONTH
                    if (isDeadlineGreaterThanNow && !isShownInMenu) {
                        isShownInMenu = true
                        mainHandler.post {
                            view?.onShouldBeShownCalendarInMenu()
                        }
                    }

                    if ((isDateGreaterThanOther(it.soft_deadline, calendarDeadlineMillisPlusMonth) || isDateGreaterThanOther(it.hard_deadline, calendarDeadlineMillisPlusMonth))
                            && isDeadlineGreaterThanNow) {
                        if (userPrefs.isNeedToShowCalendarWidget) {
                            mainHandler.post {
                                view?.onShouldBeShownCalendar(true)
                            }
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
            val calId = getFirstPrimaryCalendar() // TODO: get primary by owner and show dialog for choose user
            sectionList.filterNotNull().forEach {
                // We can choose soft_deadline or last_deadline of the Course, if section doesn't have it, but it will pollute calendar.

                // Add upcoming deadlines of sections:
                if (isDateGreaterThanOther(it.soft_deadline, nowMinus1Hour)) {
                    val deadline = it.soft_deadline
                    if (deadline != null) {
                        addDeadlineEvent(it, deadline, DeadlineType.softDeadline, addedCalendarSectionsMap[it.id], calId)
                    }
                }

                if (isDateGreaterThanOther(it.hard_deadline, nowMinus1Hour)) {
                    val deadline = it.hard_deadline
                    if (deadline != null) {
                        addDeadlineEvent(it, deadline, DeadlineType.hardDeadline, addedCalendarSectionsMap[it.id], calId)
                    }
                }
            }

            mainHandler.post {
                view?.successExported()
            }
        }
    }

    private fun getFirstPrimaryCalendar(): Long {
        val projection = arrayOf(CalendarContract.Calendars._ID/*, CalendarContract.Calendars.ACCOUNT_NAME,  CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.OWNER_ACCOUNT */, CalendarContract.Calendars.IS_PRIMARY)
        context.contentResolver.query(CalendarContract.Calendars.CONTENT_URI, projection, null, null, null).use {
            it.moveToFirst()
            while (!it.isAfterLast) {
                val indexId = it.getColumnIndex(CalendarContract.Calendars._ID)
                val indexIsPrimary = it.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)

                val isPrimary = it.getInt(indexIsPrimary) > 0
                val calendarId = it.getLong(indexId)

                if (isPrimary) {
                    return calendarId
                }

                it.moveToNext()
            }
        }
        return 1
    }

    @WorkerThread
    private fun addDeadlineEvent(section: Section, deadline: String, deadlineType: DeadlineType, calendarSection: CalendarSection?, calendarId: Long) {

        val dateEndInMillis = DateTime(deadline).millis
        val dateStartInMillis = dateEndInMillis - AppConstants.MILLIS_IN_1HOUR

        val contentValues = ContentValues()
        contentValues.put(CalendarContract.Events.DTSTART, dateStartInMillis)
        contentValues.put(CalendarContract.Events.DTEND, dateEndInMillis)

        val calendarTitle = section.title + " — " + context.getString(deadlineType.deadlineTitle)
        contentValues.put(CalendarContract.Events.TITLE, calendarTitle);
        contentValues.put(CalendarContract.Events.DESCRIPTION, StringUtil.getAbsoluteUriForSection(config, section));
        contentValues.put(CalendarContract.Events.CALENDAR_ID, calendarId)
        contentValues.put(CalendarContract.Events.EVENT_TIMEZONE, DateTimeZone.getDefault().id)
        contentValues.put(CalendarContract.Events.HAS_ALARM, 1)

        if (calendarSection != null && isEventInCal(calendarSection.eventId)) {
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, calendarSection.eventId)
            val rowsUpdated = context.contentResolver.update(uri, contentValues, null, null)
            addToDatabase(section, deadlineType, deadline, calendarSection.eventId)
        } else {
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, contentValues)

            val eventId: Long = (uri.lastPathSegment).toLong()

            addToDatabase(section, deadlineType, deadline, eventId)

            val reminderValues = ContentValues()
            reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventId)
            reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_DEFAULT)
            reminderValues.put(CalendarContract.Reminders.MINUTES, AppConstants.TWO_DAY_IN_MINUTES)
            val uriReminder = context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
        }
    }


    private fun isEventInCal(eventId: Long): Boolean {
        context.contentResolver
                .query(CalendarContract.Events.CONTENT_URI, arrayOf(CalendarContract.Events._ID), CalendarContract.Events._ID + " = ? ", arrayOf(eventId.toString()), null)
                .use {
                    if (it.moveToFirst()) {
                        return true
                    } else {
                        return false
                    }
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