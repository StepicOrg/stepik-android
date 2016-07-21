package org.stepic.droid.core

interface CalendarExportableView {
    fun permissionNotGranted()
    fun successExported()
    fun onShouldBeShownCalendar(needShow: Boolean)
}