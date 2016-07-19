package org.stepic.droid.core

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.stepic.droid.configuration.IConfig
import org.stepic.droid.model.Course
import org.stepic.droid.model.Section
import org.stepic.droid.util.AppConstants

class CalendarManagerImpl(val config: IConfig) : CalendarManager {
    override fun shouldBeShownAsWidget(sectionList: List<Section>?): Boolean {
        //TODO: check preferences
        return shouldBeShown(sectionList)
    }

    override fun shouldBeShown(sectionList: List<Section>?): Boolean {
        if (sectionList == null || sectionList.isEmpty()) return false

        val now: Long = DateTime.now(DateTimeZone.getDefault()).millis
        val nowMinus1Hour = now - AppConstants.MILLIS_IN_1HOUR

        sectionList.forEach {
            if (isDateGreaterNowMinus1Hour(it.soft_deadline, nowMinus1Hour)
                    || isDateGreaterNowMinus1Hour(it.hard_deadline, nowMinus1Hour)) {
                return true;
            }
        }
        return false
    }

    private fun isDateGreaterNowMinus1Hour(deadline: String?, nowMinus1Hour: Long): Boolean {
        if (deadline != null) {
            val deadlineDateTime = DateTime(deadline)
            val deadlineMillis = deadlineDateTime.millis
            if (deadlineMillis - nowMinus1Hour > 0) {
                return true
            } else {
                return false
            }
        } else {
            return false
        }
    }

    override fun addDeadlinesToCalendar(sectionList: List<Section>, course: Course) {

    }
}