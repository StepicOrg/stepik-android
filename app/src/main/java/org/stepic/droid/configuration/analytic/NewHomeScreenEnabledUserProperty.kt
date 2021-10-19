package org.stepic.droid.configuration.analytic

import org.stepic.droid.configuration.RemoteConfig
import org.stepik.android.domain.base.analytic.UserProperty

class NewHomeScreenEnabledUserProperty(isNewHomeScreenEnabled: Boolean) : UserProperty {
    override val name: String =
        RemoteConfig.PREFIX + RemoteConfig.IS_NEW_HOME_SCREEN_ENABLED

    override val value: Boolean =
        isNewHomeScreenEnabled
}