package org.stepic.droid.ui.activities

import android.view.MenuItem
import androidx.fragment.app.Fragment
import org.stepic.droid.R
import org.stepic.droid.ui.fragments.NotificationSettingsFragment
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.view.settings.ui.activity.SettingsActivity

class NotificationSettingsActivity : SettingsActivity() {
    override fun createFragment(): Fragment = NotificationSettingsFragment.newInstance()

    override fun setUpToolbar() {
        initCenteredToolbar(R.string.notification_settings_title,
            true,
            closeIconDrawableRes)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean =
        when (item?.itemId) {
            android.R.id.home -> {
                // Respond to the action bar's Up/Home button
                finish()
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }

}
