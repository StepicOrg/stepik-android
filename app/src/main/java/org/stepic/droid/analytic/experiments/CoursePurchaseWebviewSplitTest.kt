package org.stepic.droid.analytic.experiments

import org.stepic.droid.analytic.Analytic
import org.stepic.droid.preferences.SharedPreferenceHelper
import javax.inject.Inject

class CoursePurchaseWebviewSplitTest
@Inject
constructor(
    analytic: Analytic,
    sharedPreferenceHelper: SharedPreferenceHelper
) : SplitTest<CoursePurchaseWebviewSplitTest.Group>(
    analytic,
    sharedPreferenceHelper,

    name = "course_purchase_webview",
    groups = Group.values()
) {
    enum class Group(
        val isInAppWebViewUsed: Boolean
    ) : SplitTest.Group {
        Control(isInAppWebViewUsed = false),
        InAppWebview(isInAppWebViewUsed = true)
    }
}