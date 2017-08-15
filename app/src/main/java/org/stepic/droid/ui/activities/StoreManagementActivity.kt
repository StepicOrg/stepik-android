package org.stepic.droid.ui.activities

import org.stepic.droid.R
import org.stepic.droid.ui.fragments.StoreManagementFragment
import org.stepic.droid.ui.util.initCenteredToolbar

class StoreManagementActivity : SettingsActivity() {
    override fun createFragment() = StoreManagementFragment.newInstance()

    override fun setUpToolbar() {
        initCenteredToolbar(R.string.space_management_title,
                true,
                closeIconDrawableRes)
    }

}