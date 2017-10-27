package org.stepic.droid.ui.activities

import android.content.Intent
import android.support.annotation.NonNull
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.fragments.SectionsFragment

class SectionActivity : SingleFragmentActivity() {

    override fun createFragment() = SectionsFragment.newInstance()

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        (fragment as? SectionsFragment)?.onNewIntent(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val fragments = supportFragmentManager.fragments
        if (fragments != null) {
            for (fragment in fragments) {
                fragment?.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    override fun applyTransitionPrev() {
        //no-op
    }
}
