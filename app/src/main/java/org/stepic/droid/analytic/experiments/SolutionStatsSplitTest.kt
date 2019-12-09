package org.stepic.droid.analytic.experiments

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.preferences.SharedPreferenceHelper
import javax.inject.Inject

class SolutionStatsSplitTest
@Inject
constructor(
    analytic: Analytic,
    sharedPreferenceHelper: SharedPreferenceHelper
) : SplitTest<SolutionStatsSplitTest.Group>(
    analytic,
    sharedPreferenceHelper,

    name = "solutions_stats",
    groups = Group.values()
) {
    enum class Group(
        val isStatsVisible: Boolean
    ) : SplitTest.Group {
        Control(isStatsVisible = false),
        SolutionStats(isStatsVisible = true)
    }
}