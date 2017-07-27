package org.stepic.droid.ui.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.MenuItem
import org.stepic.droid.R
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.fragments.TextFeedbackFragment

class TextFeedbackActivity : SingleFragmentActivity() {
    override fun createFragment(): Fragment {
        return TextFeedbackFragment.Companion.newInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(null)
        setTitle(R.string.feedback_title)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(org.stepic.droid.R.anim.no_transition, org.stepic.droid.R.anim.push_down)
    }

    companion object {
        val requestCode = 17
    }
}