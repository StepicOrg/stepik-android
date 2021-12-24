package org.stepic.droid.configuration.analytic

import org.stepic.droid.configuration.RemoteConfig
import org.stepik.android.domain.base.analytic.UserProperty

class CoursePurchaseFlowProperty(purchaseFlow: String) : UserProperty {
    override val name: String =
        RemoteConfig.PREFIX + RemoteConfig.PURCHASE_FLOW_ANDROID

    override val value: String =
        purchaseFlow
}