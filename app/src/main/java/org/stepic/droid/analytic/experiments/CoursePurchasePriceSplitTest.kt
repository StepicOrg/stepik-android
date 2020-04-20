package org.stepic.droid.analytic.experiments

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.preferences.SharedPreferenceHelper
import javax.inject.Inject

class CoursePurchasePriceSplitTest
@Inject
constructor(
    analytic: Analytic,
    sharedPreferenceHelper: SharedPreferenceHelper
) : SplitTest<CoursePurchasePriceSplitTest.Group>(
    analytic,
    sharedPreferenceHelper,

    name = "course_purchase_price",
    groups = Group.values()
) {
    enum class Group(
        val isPriceVisible: Boolean
    ) : SplitTest.Group {
        Control(isPriceVisible = false),
        CoursePrice(isPriceVisible = true)
    }
}