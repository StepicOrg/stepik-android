package org.stepic.droid.view.activities

import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.view.fragments.CommentsFragment

class CommentsActivity : SingleFragmentActivity() {

    override fun createFragment() = CommentsFragment.newInstance()

    override fun finish() {
        super.finish()
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.push_down)
    }
}