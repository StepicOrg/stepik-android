package org.stepic.droid.analytic.experiments

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.preferences.SharedPreferenceHelper
import javax.inject.Inject

class AchievementsSplitTest
@Inject
constructor(
    analytics: Analytic,
    sharedPreferenceHelper: SharedPreferenceHelper
) : SplitTest<AchievementsSplitTest.Group>(
    analytics,
    sharedPreferenceHelper,

    name = "achievements",
    groups = Group.values()
) {

    enum class Group(
        val isAchievementsEnabled: Boolean
    ) : SplitTest.Group {
        Control(isAchievementsEnabled = true),
        NoAchievements(isAchievementsEnabled = false)
    }
}