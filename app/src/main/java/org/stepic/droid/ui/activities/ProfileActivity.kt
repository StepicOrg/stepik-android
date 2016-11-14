package org.stepic.droid.ui.activities

import android.net.Uri
import android.support.v4.app.Fragment
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.fragments.ProfileFragment

class ProfileActivity : SingleFragmentActivity() {

    override fun createFragment(): Fragment? {
        val dataUri = intent?.data
        val userId = getUserId(dataUri)
        return ProfileFragment.newInstance(userId)
    }

    private fun getUserId(dataUri: Uri?): Long {
        if (dataUri == null) return 0;
        val pathSegments = dataUri.pathSegments
        try {
            return pathSegments[1].toLong()
        } catch (exception: NumberFormatException) {
            return -1;
        }
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
