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
        val isDeferredAuth: Boolean,
        val isCanDismissLaunch: Boolean,
        override val distribution: Int
    ) : SplitTest.Group {
        Control(isDeferredAuth = false, isCanDismissLaunch = false, distribution = 2),
        DeferredAuthGroup1(isDeferredAuth = true, isCanDismissLaunch = true, distribution = 1),
        DeferredAuthGroup2(isDeferredAuth = true, isCanDismissLaunch = false, distribution = 1)
    }
}