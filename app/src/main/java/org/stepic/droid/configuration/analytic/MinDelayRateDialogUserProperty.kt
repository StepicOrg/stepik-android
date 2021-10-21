package org.stepic.droid.configuration.analytic

import org.stepic.droid.configuration.RemoteConfig
import org.stepik.android.domain.base.analytic.UserProperty

class MinDelayRateDialogUserProperty(delay: Long) : UserProperty {
    override val name: String =
        RemoteConfig.PREFIX + RemoteConfig.MIN_DELAY_RATE_DIALOG_SEC

    override val value: Long =
        delay
}