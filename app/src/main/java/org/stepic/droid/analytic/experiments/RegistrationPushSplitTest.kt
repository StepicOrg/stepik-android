package org.stepic.droid.analytic.experiments

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.preferences.SharedPreferenceHelper
import javax.inject.Inject

class RegistrationPushSplitTest
@Inject
constructor(
    analytics: Analytic,
    sharedPreferenceHelper: SharedPreferenceHelper
) : SplitTest<RegistrationPushSplitTest.Group>(
    analytics,
    sharedPreferenceHelper,

    name = "registration_push",
    groups = Group.values()
) {
    enum class Group(
        val isPushEnabled: Boolean
    ) : SplitTest.Group {
        Control(isPushEnabled = true),
        NoPush(isPushEnabled = false)
    }
}