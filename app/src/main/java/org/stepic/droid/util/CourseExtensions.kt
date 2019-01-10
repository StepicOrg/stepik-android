package org.stepic.droid.util

import org.stepik.android.model.Course

val Course.canContinue: Boolean
    get() = totalUnits != 0L
            && scheduleType != Course.SCHEDULE_TYPE_UPCOMMING
            && scheduleType != Course.SCHEDULE_TYPE_ENDED