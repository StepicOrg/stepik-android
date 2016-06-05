package org.stepic.droid.view.activities

import android.support.v4.app.Fragment
import android.view.MenuItem
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.view.fragments.NewCommentFragment

class NewCommentActivity : SingleFragmentActivity() {

    companion object {
        val keyTarget = "KEY_target_id"
        val keyParent = "KEY_Parent_id"
    }

    override fun createFragment(): Fragment? {
        val target: Long = intent.extras.getLong(NewCommentActivity.keyTarget)
        var parent: Long? = intent.extras.getLong(NewCommentActivity.keyParent)
        if (parent == 0L) {
            parent = null
        }
        return NewCommentFragment.newInstance(target, parent)
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