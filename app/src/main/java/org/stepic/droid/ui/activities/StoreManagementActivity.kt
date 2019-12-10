package org.stepic.droid.ui.activities

import org.stepic.droid.R
import org.stepic.droid.ui.fragments.StoreManagementFragment
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepik.android.view.settings.ui.activity.SettingsActivity

class StoreManagementActivity : SettingsActivity() {
    override fun createFragment() = StoreManagementFragment.newInstance()

    override fun setUpToolbar() {
        initCenteredToolbar(R.string.space_management_title,
                true,
                closeIconDrawableRes)
    }

}