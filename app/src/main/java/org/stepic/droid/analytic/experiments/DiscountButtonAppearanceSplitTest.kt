package org.stepic.droid.analytic.experiments

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.preferences.SharedPreferenceHelper
import javax.inject.Inject

class DiscountButtonAppearanceSplitTest
@Inject
constructor(
    analytic: Analytic,
    sharedPreferenceHelper: SharedPreferenceHelper
) : SplitTest<DiscountButtonAppearanceSplitTest.Group>(
    analytic,
    sharedPreferenceHelper,

    name = "discount_appearance",
    groups = Group.values()
) {
    enum class Group : SplitTest.Group {
        DiscountTransparent,
        DiscountGreen,
        DiscountPurple
    }
}