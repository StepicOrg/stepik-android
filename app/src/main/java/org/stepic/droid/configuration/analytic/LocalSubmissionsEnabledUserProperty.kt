package org.stepic.droid.configuration.analytic

import org.stepic.droid.configuration.RemoteConfig
import org.stepik.android.domain.base.analytic.UserProperty

class LocalSubmissionsEnabledUserProperty(isLocalSubmissionsEnabled: Boolean) : UserProperty {
    override val name: String =
        RemoteConfig.PREFIX + RemoteConfig.IS_LOCAL_SUBMISSIONS_ENABLED

    override val value: Boolean =
        isLocalSubmissionsEnabled
}