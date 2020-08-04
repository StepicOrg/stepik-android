package org.stepic.droid.analytic.experiments

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.preferences.SharedPreferenceHelper
import javax.inject.Inject

class InAppPurchaseSplitTest
@Inject
constructor(
  analytic: Analytic,
  sharedPreferenceHelper: SharedPreferenceHelper
) : SplitTest<InAppPurchaseSplitTest.Group>(
    analytic,
    sharedPreferenceHelper,

    name = "in_app_purchase",
    groups = Group.values()
) {
    enum class Group(
        val isInAppPurchaseActive: Boolean
    ) : SplitTest.Group {
        Control(isInAppPurchaseActive = true),
        InAppPurchase(isInAppPurchaseActive = true)
    }
}