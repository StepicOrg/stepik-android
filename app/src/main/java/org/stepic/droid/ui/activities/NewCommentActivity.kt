package org.stepic.droid.ui.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.MenuItem
import org.stepic.droid.R
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.fragments.NewCommentFragment
import org.stepic.droid.ui.util.BackButtonHandler
import org.stepic.droid.ui.util.OnBackClickListener
import java.lang.ref.WeakReference

class NewCommentActivity : SingleFragmentActivity(), BackButtonHandler {
    var onBackClickListener: WeakReference<OnBackClickListener>? = null

    override fun setBackClickListener(onBackClickListener: OnBackClickListener) {
        this.onBackClickListener = WeakReference(onBackClickListener)
    }

    override fun removeBackClickListener(onBackClickListener: OnBackClickListener) {
        this.onBackClickListener = null
    }

    companion object {
        val keyTarget = "KEY_target_id"
        val keyParent = "KEY_Parent_id"
        val keyComment = "keyComment"
        val requestCode = 171
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(null)
        setTitle(R.string.comment_compose_title)
    }

    override fun createFragment(): Fragment {
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
                if (fragmentBackKeyIntercept()) {
                    return true
                } else {
                    finish();
                    return true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.push_down)
    }

    override fun onBackPressed() {
        if (!fragmentBackKeyIntercept()) {
            super.onBackPressed()
        }
    }

    private fun fragmentBackKeyIntercept(): Boolean {
        return onBackClickListener?.get()?.onBackClick() ?: false
    }
}