package org.stepic.droid.analytic.experiments

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.preferences.SharedPreferenceHelper
import javax.inject.Inject

class PersonalDeadlinesSplitTest
@Inject
constructor(
    analytics: Analytic,
    sharedPreferenceHelper: SharedPreferenceHelper
) : SplitTest<PersonalDeadlinesSplitTest.Group>(
    analytics,
    sharedPreferenceHelper,

    name = "personal_deadlines",
    groups = Group.values()
) {
    enum class Group(
        val isPersonalDeadlinesEnabled: Boolean
    ) : SplitTest.Group {
        Control(isPersonalDeadlinesEnabled = true),
        NoDeadLines(isPersonalDeadlinesEnabled = false)
    }
}