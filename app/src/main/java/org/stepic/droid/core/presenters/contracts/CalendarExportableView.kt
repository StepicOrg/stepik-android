package org.stepic.droid.core.presenters.contracts

import org.stepic.droid.model.CalendarItem
import java.util.*

interface CalendarExportableView {

    fun permissionNotGranted()

    fun successExported()

    fun hideCalendarAfterNotNow()

    fun onShouldBeShownCalendar(needShow: Boolean)

    fun onShouldBeShownCalendarInMenu()

    fun onNeedToChooseCalendar(primariesCalendars: ArrayList<CalendarItem>)

    fun onUserDoesntHaveCalendar()
}