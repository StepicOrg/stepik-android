package org.stepic.droid.configuration.analytic

import org.stepic.droid.configuration.RemoteConfig
import org.stepik.android.domain.base.analytic.UserProperty

class AdaptiveBackendUrlUserProperty(adaptiveBackendUrl: String) : UserProperty {
    override val name: String =
        RemoteConfig.PREFIX + RemoteConfig.ADAPTIVE_BACKEND_URL

    override val value: String =
        adaptiveBackendUrl
}