package org.stepic.droid.analytic.experiments

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.preferences.SharedPreferenceHelper
import javax.inject.Inject

class DeferredAuthSplitTest
@Inject
constructor(
    analytic: Analytic,
    sharedPreferenceHelper: SharedPreferenceHelper
) : SplitTest<DeferredAuthSplitTest.Group>(
    analytic,
    sharedPreferenceHelper,

    name = "deferred_auth",
    groups = Group.values()
) {
    enum class Group(
        val isDeferredAuth: Boolean
    ) : SplitTest.Group {
        Control(isDeferredAuth = false),
        DeferredAuth(isDeferredAuth = true)
    }
}