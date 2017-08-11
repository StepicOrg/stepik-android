package org.stepic.droid.ui.activities

import android.os.Bundle
import org.stepic.droid.R
import org.stepic.droid.ui.fragments.StoreManagementFragment

class StoreManagementActivity : SettingsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.space_management_title)
    }

    override fun createFragment() = StoreManagementFragment.newInstance()
}