package org.stepic.droid.ui.activities

import android.support.v4.app.Fragment
import android.view.MenuItem
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.fragments.CommentsFragment

class CommentsActivity : SingleFragmentActivity() {

    companion object {
        val keyDiscussionProxyId = "KEY_DISCUSSION_PROXY_ID"
        val keyStepId = "KEY_step_id"
        val keyNeedInstaOpenForm = "key_need_insta_open"
    }

    override fun createFragment(): Fragment {
        val discussionId: String = intent.extras.getString(keyDiscussionProxyId)
        val stepId: Long = intent.extras.getLong(keyStepId)
        val needInstaOpen: Boolean = intent.extras.getBoolean(keyNeedInstaOpenForm)
        return CommentsFragment.newInstance(discussionId, stepId, needInstaOpen)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish();
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.push_down)
    }

}