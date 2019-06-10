package org.stepic.droid.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import org.stepic.droid.R
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.fragments.FeedbackFragment
import org.stepic.droid.ui.util.initCenteredToolbar

class FeedbackActivity : SingleFragmentActivity() {
    override fun createFragment() = FeedbackFragment.newInstance()

    override fun getLayoutResId(): Int {
        return R.layout.activity_container_with_bar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(null)
        setUpToolbar()
    }


    private fun setUpToolbar() {
        initCenteredToolbar(R.string.feedback_title,
                showHomeButton = true,
                homeIndicator = closeIconDrawableRes)
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
