package org.stepic.droid.ui.activities

import android.support.annotation.NonNull
import org.stepic.droid.base.SingleFragmentActivity

class SectionActivity : SingleFragmentActivity() {

    override fun createFragment() = TODO()

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
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
