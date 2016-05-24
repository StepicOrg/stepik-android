package org.stepic.droid.view.activities

import android.support.v4.app.Fragment
import org.stepic.droid.base.SingleFragmentActivity

class CommentsActivity : SingleFragmentActivity() {
    override fun createFragment(): Fragment? {
        throw UnsupportedOperationException()
    }


    override fun finish() {
        super.finish()
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.push_down)
    }
}