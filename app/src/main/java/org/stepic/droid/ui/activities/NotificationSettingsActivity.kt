package org.stepic.droid.ui.activities

import android.support.v4.app.Fragment
import org.stepic.droid.R
import org.stepic.droid.ui.fragments.NotificationSettingsFragment
import org.stepic.droid.ui.util.initCenteredToolbar

class NotificationSettingsActivity : SettingsActivity() {
    override fun createFragment(): Fragment = NotificationSettingsFragment.newInstance()

    override fun setUpToolbar() {
        initCenteredToolbar(R.string.notification_settings_title,
                true,
                closeIconDrawableRes)
    }
}
