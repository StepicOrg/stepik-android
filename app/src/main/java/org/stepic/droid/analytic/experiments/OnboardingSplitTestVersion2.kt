package org.stepic.droid.analytic.experiments

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.preferences.SharedPreferenceHelper
import javax.inject.Inject

class OnboardingSplitTestVersion2
@Inject
constructor(
    analytic: Analytic,
    sharedPreferenceHelper: SharedPreferenceHelper
) : SplitTest<OnboardingSplitTestVersion2.Group>(
    analytic,
    sharedPreferenceHelper,

    name = "personalized_onboarding_2",
    groups = Group.values()
) {
    enum class Group : SplitTest.Group {
        Control,
        Personalized,
        None,
        ControlPersonalized
    }
}