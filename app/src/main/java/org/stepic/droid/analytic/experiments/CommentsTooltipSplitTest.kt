package org.stepic.droid.analytic.experiments

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.preferences.SharedPreferenceHelper
import javax.inject.Inject

class CommentsTooltipSplitTest
@Inject
constructor(
    analytics: Analytic,
    sharedPreferenceHelper: SharedPreferenceHelper
) : SplitTest<CommentsTooltipSplitTest.Group>(
    analytics,
    sharedPreferenceHelper,

    name = "comments",
    groups =  Group.values()
) {
    enum class Group(
        val isCommentsToolTipEnabled: Boolean
    ) : SplitTest.Group {
        Control(isCommentsToolTipEnabled = false),
        TooltipEnabled(isCommentsToolTipEnabled = true)
    }
}