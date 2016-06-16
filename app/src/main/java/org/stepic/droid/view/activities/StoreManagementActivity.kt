package org.stepic.droid.view.activities

import android.support.v4.app.Fragment
import org.stepic.droid.view.fragments.StoreManagementFragment

class StoreManagementActivity : SettingsActivity() {
    override fun createFragment(): Fragment? {
        return StoreManagementFragment.newInstance()
    }
}