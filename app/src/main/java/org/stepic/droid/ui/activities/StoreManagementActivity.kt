package org.stepic.droid.ui.activities

import android.support.v4.app.Fragment
import org.stepic.droid.ui.fragments.StoreManagementFragment

class StoreManagementActivity : SettingsActivity() {
    override fun createFragment(): Fragment? {
        return StoreManagementFragment.newInstance()
    }
}