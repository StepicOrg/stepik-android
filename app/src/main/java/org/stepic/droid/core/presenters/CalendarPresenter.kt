package org.stepic.droid.core.presenters

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.CalendarContract
import android.support.annotation.WorkerThread
import android.support.v4.content.ContextCompat
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.configuration.Config
import org.stepic.droid.core.DeadlineType
import org.stepic.droid.core.presenters.contracts.CalendarExportableView
import org.stepic.droid.di.course.CourseAndSectionsScope
import org.stepic.droid.model.CalendarItem
import org.stepic.droid.model.CalendarSection
import org.stepik.android.model.structure.Section
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.StringUtil
import java.util.*
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@CourseAndSectionsScope
class CalendarPresenter
@Inject
constructor(
        private val config: Config,
        private val mainHandler: MainHandler,
        private val context: Context,
        private val threadPool: ThreadPoolExecutor,
        private val database: DatabaseFacade,
        private val userPrefs: UserPreferences,
        private val analytic: Analytic
) : PresenterBase<CalendarExportableView>() {

    /**
     * true, if any section in oldList have deadline (soft or hard) and the deadline is not happened
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
            val now: Long = DateTimeHelper.nowLocal()
            val nowMinus1Hour = now - AppConstants.MILLIS_IN_1HOUR

            val ids = sectionList
                    .map { it.id }
                    .toLongArray()

            val addedCalendarSectionsMap = database.getCalendarSectionsByIds(ids)
            var isShownInMenu = false
            sectionList.forEach {
                val calendarSection: CalendarSection? = addedCalendarSectionsMap[it.id]
                // We can't check calendar permission, when we want to show widget
                val isDeadlineGreaterThanNow = isDeadlineAfterDate(it.softDeadline, nowMinus1Hour) || isDeadlineAfterDate(it.hardDeadline, nowMinus1Hour)

                if (calendarSection == null) {
                    if (isDeadlineGreaterThanNow) {
                        if (userPrefs.isNeedToShowCalendarWidget) {
                            analytic.reportEvent(Analytic.Calendar.SHOW_CALENDAR_AS_WIDGET)
                            mainHandler.post {
                                view?.onShouldBeShownCalendar(true)
                            }
                        } else {
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

                    val lastDeadline: Date? = calendarSection.hardDeadline ?: calendarSection.softDeadline //2017-01-06T15:59:06Z format
                    var calendarDeadlinePlusMonthUtc = Long.MAX_VALUE
                    if (lastDeadline != null) {
                        calendarDeadlinePlusMonthUtc = lastDeadline.time + AppConstants.MILLIS_IN_1MONTH
                    }

                    if ((isDeadlineAfterDate(it.softDeadline, calendarDeadlinePlusMonthUtc) || isDeadlineAfterDate(it.softDeadline, calendarDeadlinePlusMonthUtc)) && isDeadlineGreaterThanNow) {
                        if (userPrefs.isNeedToShowCalendarWidget) {
                            analytic.reportEvent(Analytic.Calendar.SHOW_CALENDAR_AS_WIDGET)
                            mainHandler.post {
                                view?.onShouldBeShownCalendar(true)
                            }
                        } else {
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

    private fun isDeadlineAfterDate(deadline: Date?, dateUtcMillis: Long): Boolean {
        if (deadline == null) {
            return false
        }

        return deadline.time - dateUtcMillis > 0
    }

    fun clickNotNow() {
        threadPool.execute {
            userPrefs.isNeedToShowCalendarWidget = false
            mainHandler.post {
                view?.hideCalendarAfterNotNow()
            }
        }
    }

    /**
     * add soft and hard deadline to calendar, if permission not granted put it to {@code exportableView}
     *
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
            val now: Long = DateTimeHelper.nowUtc()
            val nowMinus1Hour = now - AppConstants.MILLIS_IN_1HOUR
            val ids = sectionList
                    .map { it.id }
                    .toLongArray()

            val addedCalendarSectionsMap = database.getCalendarSectionsByIds(ids)
            var calendarItem: CalendarItem? = null
            if (calendarItemOut == null) {
                val primariesCalendars = getListOfPrimariesCalendars()
                when (primariesCalendars.size) {
                    0 -> {
                        analytic.reportEvent(Analytic.Calendar.CALENDAR_ADDED_FAIL)
                        mainHandler.post {
                            view?.onUserDoesntHaveCalendar()
                        }
                    }
                    1 -> calendarItem = primariesCalendars[0]
                    else -> {
                        mainHandler.post {
                            view?.onNeedToChooseCalendar(primariesCalendars)
                        }
                        return@execute
                    }
                }
            } else {
                calendarItem = calendarItemOut
            }
            val calendarItemFinal = calendarItem ?: return@execute


            sectionList.filterNotNull().forEach {
                // We can choose softDeadline or last_deadline of the Course, if section doesn't have it, but it will pollute calendar.

                // Add upcoming deadlines of sections:
                if (isDeadlineAfterDate(it.softDeadline, nowMinus1Hour)) {
                    val deadline = it.softDeadline
                    if (deadline != null) {
                        addDeadlineEvent(it, deadline, DeadlineType.softDeadline, addedCalendarSectionsMap[it.id], calendarItemFinal)
                    }
                }

                if (isDeadlineAfterDate(it.hardDeadline, nowMinus1Hour)) {
                    val deadline = it.hardDeadline
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

    @WorkerThread
    private fun getListOfPrimariesCalendars(): ArrayList<CalendarItem> {
        val listOfCalendarItems = ArrayList<CalendarItem>()
        context.contentResolver.query(CalendarContract.Calendars.CONTENT_URI, null, null, null, null).use {
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
        return listOfCalendarItems
    }

    @WorkerThread
    private fun addDeadlineEvent(section: Section, deadline: Date, deadlineType: DeadlineType, calendarSection: CalendarSection?, calendarItem: CalendarItem) {
        val dateEndInMillis = deadline.time //UTC
        val dateStartInMillis = dateEndInMillis - AppConstants.MILLIS_IN_1HOUR

        val contentValues = ContentValues()
        contentValues.put(CalendarContract.Events.DTSTART, dateStartInMillis)
        contentValues.put(CalendarContract.Events.DTEND, dateEndInMillis)

        val calendarTitle = section.title + " — " + context.getString(deadlineType.deadlineTitle)
        contentValues.put(CalendarContract.Events.TITLE, calendarTitle)
        contentValues.put(CalendarContract.Events.DESCRIPTION, StringUtil.getAbsoluteUriForSection(config, section))
        contentValues.put(CalendarContract.Events.CALENDAR_ID, calendarItem.calendarId)
        contentValues.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        contentValues.put(CalendarContract.Events.HAS_ALARM, 1)


        val eventIdInDb = calendarSection?.getEventIdBasedOnType(deadlineType)

        if (eventIdInDb != null && isEventInAnyCal(eventIdInDb)) {
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventIdInDb)
            context.contentResolver.update(uri, contentValues, null, null)
            addToDatabase(section, deadlineType, eventIdInDb)
        } else {
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, contentValues)

            val eventId: Long = (uri.lastPathSegment).toLong()

            addToDatabase(section, deadlineType, eventId)

            val reminderValues = ContentValues()
            reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventId)
            reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_DEFAULT)
            reminderValues.put(CalendarContract.Reminders.MINUTES, AppConstants.TWO_DAY_IN_MINUTES)
            context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
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


    private fun addToDatabase(section: Section, deadlineType: DeadlineType, eventId: Long) {
        val infoFromDb = database.getCalendarEvent(section.id)
        if (deadlineType == DeadlineType.softDeadline) {
            database.addCalendarEvent(CalendarSection(section.id, infoFromDb?.eventIdHardDeadline, eventId, infoFromDb?.hardDeadline, section.softDeadline))
        } else {
            database.addCalendarEvent(CalendarSection(section.id, eventId, infoFromDb?.eventIdSoftDeadline, section.hardDeadline, infoFromDb?.softDeadline))
        }
    }
}