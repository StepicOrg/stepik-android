package org.stepic.droid.configuration.analytic

import org.stepic.droid.configuration.RemoteConfig
import org.stepik.android.domain.base.analytic.UserProperty

class ShowStreakDialogAfterLoginUserProperty(showStreak: Boolean) : UserProperty {
    override val name: String =
        RemoteConfig.PREFIX + RemoteConfig.SHOW_STREAK_DIALOG_AFTER_LOGIN

    override val value: Boolean =
        showStreak
}