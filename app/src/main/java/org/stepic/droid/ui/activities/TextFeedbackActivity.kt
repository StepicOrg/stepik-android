package org.stepic.droid.ui.activities

import android.os.Bundle
import android.view.MenuItem
import org.stepic.droid.R
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.fragments.TextFeedbackFragment

class TextFeedbackActivity : SingleFragmentActivity() {

    override fun createFragment() = TextFeedbackFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(null)
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
        overridePendingTransition(R.anim.no_transition, R.anim.push_down)
    }

    companion object {
        val requestCode = 17
    }
}