package org.stepic.droid.analytic.experiments

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.preferences.SharedPreferenceHelper
import javax.inject.Inject

class CoursePurchaseReminderSplitTest
@Inject
constructor(
    analytic: Analytic,
    sharedPreferenceHelper: SharedPreferenceHelper
) : SplitTest<CoursePurchaseReminderSplitTest.Group>(
    analytic,
    sharedPreferenceHelper,

    name = "course_purchase_reminder",
    groups = Group.values()
) {
    enum class Group(
        val notificationDelayHours: Int
    ) : SplitTest.Group {
        Control(notificationDelayHours = -1),
        OneHourDelay(notificationDelayHours = 1),
        FourHourDelay(notificationDelayHours = 4)
    }
}