package org.stepic.droid.ui.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.MenuItem
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.fragments.CommentsFragment

class CommentsActivity : SingleFragmentActivity() {

    companion object {
        val keyDiscusionProxyId = "KEY_DISCUSSION_PROXY_ID"
        val keyStepId = "KEY_step_id"
    }

    override fun createFragment(): Fragment {
        val discussionId: String = intent.extras.getString(keyDiscusionProxyId)
        val stepId: Long = intent.extras.getLong(keyStepId)
        return CommentsFragment.newInstance(discussionId,stepId )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                if (mSharedPreferenceHelper.authResponseFromStore == null) {
                    finish();
                    return true
                } else {
                    return super.onOptionsItemSelected(item)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.push_down)
    }
}