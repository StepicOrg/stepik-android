package org.stepic.droid.view.activities

import android.support.v4.app.Fragment
import org.stepic.droid.view.fragments.SpaceManagementFragment

class SpaceManagementActivity : SettingsActivity() {
    override fun createFragment(): Fragment? {
        return SpaceManagementFragment.newInstance()
    }
}