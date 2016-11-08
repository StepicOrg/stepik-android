package org.stepic.droid.ui.activities

import android.support.v4.app.Fragment
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.fragments.ProfileFragment

class ProfileActivity : SingleFragmentActivity() {

    override fun createFragment(): Fragment? {
        return ProfileFragment.newInstance()
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.push_down)
    }

}
