package org.stepic.droid.analytic.experiments

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.preferences.SharedPreferenceHelper
import javax.inject.Inject

class OnboardingSplitTest
@Inject
constructor(
    analytic: Analytic,
    sharedPreferenceHelper: SharedPreferenceHelper
) : SplitTest<OnboardingSplitTest.Group>(
    analytic,
    sharedPreferenceHelper,

    name = "personalized_onboarding",
    groups = Group.values()
) {
    enum class Group : SplitTest.Group {
        Control,
        Personalized,
        None
    }
}