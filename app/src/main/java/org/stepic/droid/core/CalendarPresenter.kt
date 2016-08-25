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
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.IMainHandler
import org.stepic.droid.configuration.IConfig
import org.stepic.droid.core.presenters.PresenterBase
import org.stepic.droid.model.CalendarItem
import org.stepic.droid.model.CalendarSection
import org.stepic.droid.model.Section
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.StringUtil
import java.util.*
import java.util.concurrent.ThreadPoolExecutor

class CalendarPresenter(val config: IConfig,
                            val mainHandler: IMainHandler,
                            val context: Context,
                            val threadPool: ThreadPoolExecutor,
                            val database: DatabaseFacade,
                            val userPrefs: UserPreferences,
                            val analytic : Analytic) : PresenterBase<CalendarExportableView>() {

    /**
     * true, if any section in list have deadline (soft or hard) and the deadline is not happened
     * and calendar was not added before. If course was restarted, than show (new_deadline - old_deadline > 1 month).
     * false, otherwise.
     *
     */
    fun checkToShowCalendar(outSectionList: List<Section>?) {
        if (outSectionList == null || outSectionList.isEmpty()) {
            view?.onShouldBeShownCalendar(false)
            return
        }
        val sectionList = ArrayList(outSectionList)

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
                            analytic.reportEvent(Analytic.Calendar.SHOW_CALENDAR_AS_WIDGET)
                            mainHandler.post {
                                view?.onShouldBeShownCalendar(true)
                            }
                        }
                        else{
                            analytic.reportEvent(Analytic.Calendar.HIDE_WIDGET_FROM_PREFS)
                        }

                        if (!isShownInMenu) {
                            analytic.reportEvent(Analytic.Calendar.SHOW_CALENDAR)
                            isShownInMenu = true
                            mainHandler.post {
                                view?.onShouldBeShownCalendarInMenu()
                            }
                        }
                        return@execute
                    }
                } else {
                    // we already exported in calendar this section! Check if new date in future and greater that calendar date + 30 days
                    if (isDeadlineGreaterThanNow && !isShownInMenu) {
                        analytic.reportEvent(Analytic.Calendar.SHOW_CALENDAR)
                        isShownInMenu = true
                        mainHandler.post {
                            view?.onShouldBeShownCalendarInMenu()
                        }
                    }

                    val lastDeadline = calendarSection.hardDeadline ?: calendarSection.softDeadline
                    var calendarDeadlineMillisPlusMonth = Long.MAX_VALUE
                    if (lastDeadline != null) {
                        calendarDeadlineMillisPlusMonth = DateTime(lastDeadline).millis + AppConstants.MILLIS_IN_1MONTH
                    }

                    if ((isDateGreaterThanOther(it.soft_deadline, calendarDeadlineMillisPlusMonth) || isDateGreaterThanOther(it.hard_deadline, calendarDeadlineMillisPlusMonth))
                            && isDeadlineGreaterThanNow) {
                        if (userPrefs.isNeedToShowCalendarWidget) {
                            analytic.reportEvent(Analytic.Calendar.SHOW_CALENDAR_AS_WIDGET)
                            mainHandler.post {
                                view?.onShouldBeShownCalendar(true)
                            }
                        }
                        else{
                            analytic.reportEvent(Analytic.Calendar.HIDE_WIDGET_FROM_PREFS)
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

    /**
     * add soft and hard deadline to calendar, if permission not granted put it to {@code exportableView}
     *
     * @param sectionList list of sections of course
     */
    fun addDeadlinesToCalendar(outSectionList: List<Section>, calendarItemOut: CalendarItem?) {
        val permissionCheck = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_CALENDAR)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            view?.permissionNotGranted()
            return
        }
        val sectionList = ArrayList(outSectionList)

        threadPool.execute {
            val now: Long = DateTime.now(DateTimeZone.getDefault()).millis
            val nowMinus1Hour = now - AppConstants.MILLIS_IN_1HOUR
            val ids = sectionList
                    .map { it.id }
                    .toLongArray()

            val addedCalendarSectionsMap = database.getCalendarSectionsByIds(ids)
            var calendarItem: CalendarItem? = null
            if (calendarItemOut == null) {
                val primariesCalendars = getListOfPrimariesCalendars()
                if (primariesCalendars.size == 1) {
                    calendarItem = primariesCalendars.get(0)
                } else if (primariesCalendars.size > 1) {
                    mainHandler.post {
                        view?.onNeedToChooseCalendar(primariesCalendars)
                    }
                    return@execute
                }
                else if (primariesCalendars.isEmpty()){
                    analytic.reportEvent(Analytic.Calendar.CALENDAR_ADDED_FAIL)
                    mainHandler.post {
                        view?.onUserDoesntHaveCalendar()
                    }
                }
            } else {
                calendarItem = calendarItemOut
            }
            val calendarItemFinal = calendarItem ?: return@execute


            sectionList.filterNotNull().forEach {
                // We can choose soft_deadline or last_deadline of the Course, if section doesn't have it, but it will pollute calendar.

                // Add upcoming deadlines of sections:
                if (isDateGreaterThanOther(it.soft_deadline, nowMinus1Hour)) {
                    val deadline = it.soft_deadline
                    if (deadline != null) {
                        addDeadlineEvent(it, deadline, DeadlineType.softDeadline, addedCalendarSectionsMap[it.id], calendarItemFinal)
                    }
                }

                if (isDateGreaterThanOther(it.hard_deadline, nowMinus1Hour)) {
                    val deadline = it.hard_deadline
                    if (deadline != null) {
                        addDeadlineEvent(it, deadline, DeadlineType.hardDeadline, addedCalendarSectionsMap[it.id], calendarItemFinal)
                    }
                }
            }

            analytic.reportEvent(Analytic.Calendar.CALENDAR_ADDED_SUCCESSFULLY)
            mainHandler.post {
                view?.successExported()
            }
        }
    }

    private fun getListOfPrimariesCalendars(): ArrayList<CalendarItem> {
        val listOfCalendarItems = ArrayList<CalendarItem>()
        val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.OWNER_ACCOUNT, CalendarContract.Calendars.IS_PRIMARY)
        context.contentResolver.query(CalendarContract.Calendars.CONTENT_URI, projection, null, null, null).use {
            it.moveToFirst()
            while (!it.isAfterLast) {
                val indexId = it.getColumnIndex(CalendarContract.Calendars._ID)
                var indexIsPrimary = it.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)
                val indexOwner = it.getColumnIndex(CalendarContract.Calendars.OWNER_ACCOUNT)
                if (indexIsPrimary < 0){
                    indexIsPrimary = it.getColumnIndex("COALESCE(isPrimary, ownerAccount = account_name)")//look at http://stackoverflow.com/questions/25870556/check-if-calendar-is-primary
                }

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
        return listOfCalendarItems
    }

    @WorkerThread
    private fun addDeadlineEvent(section: Section, deadline: String, deadlineType: DeadlineType, calendarSection: CalendarSection?, calendarItem: CalendarItem) {

        val dateEndInMillis = DateTime(deadline).millis
        val dateStartInMillis = dateEndInMillis - AppConstants.MILLIS_IN_1HOUR

        val contentValues = ContentValues()
        contentValues.put(CalendarContract.Events.DTSTART, dateStartInMillis)
        contentValues.put(CalendarContract.Events.DTEND, dateEndInMillis)

        val calendarTitle = section.title + " — " + context.getString(deadlineType.deadlineTitle)
        contentValues.put(CalendarContract.Events.TITLE, calendarTitle);
        contentValues.put(CalendarContract.Events.DESCRIPTION, StringUtil.getAbsoluteUriForSection(config, section));
        contentValues.put(CalendarContract.Events.CALENDAR_ID, calendarItem.calendarId)
        contentValues.put(CalendarContract.Events.EVENT_TIMEZONE, DateTimeZone.getDefault().id)
        contentValues.put(CalendarContract.Events.HAS_ALARM, 1)


        val eventIdInDb = calendarSection?.getEventIdBasedOnType(deadlineType)

        if (eventIdInDb != null && isEventInAnyCal(eventIdInDb)) {
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventIdInDb)
            val rowsUpdated = context.contentResolver.update(uri, contentValues, null, null)
            addToDatabase(section, deadlineType, deadline, eventIdInDb)
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


    private fun isEventInAnyCal(eventId: Long): Boolean {
        context.contentResolver
                .query(CalendarContract.Events.CONTENT_URI, arrayOf(CalendarContract.Events._ID, CalendarContract.Events.CALENDAR_ID), CalendarContract.Events._ID + " = ? ", arrayOf(eventId.toString()), null)
                .use {
                    it.moveToFirst()
                    if (!it.isAfterLast) {
                        return true
                    }
                    return false
                }
    }


    fun addToDatabase(section: Section, deadlineType: DeadlineType, deadline: String, eventId: Long) {
        val infoFromDb = database.getCalendarEvent(section.id)
        if (deadlineType == DeadlineType.softDeadline) {
            database.addCalendarEvent(CalendarSection(section.id, infoFromDb?.eventIdHardDeadline, eventId, infoFromDb?.hardDeadline, section.soft_deadline))
        } else {
            database.addCalendarEvent(CalendarSection(section.id, eventId, infoFromDb?.eventIdSoftDeadline, section.hard_deadline, infoFromDb?.softDeadline))
        }
    }
}