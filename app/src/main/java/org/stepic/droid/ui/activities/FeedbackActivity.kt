package org.stepic.droid.ui.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import org.stepic.droid.R
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.ui.fragments.FeedbackFragment
import org.stepic.droid.ui.util.initCenteredToolbar

class FeedbackActivity : SingleFragmentActivity() {
    override fun createFragment(): Fragment =
        FeedbackFragment.newInstance()

    override fun getLayoutResId(): Int =
        R.layout.activity_container_with_bar

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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean =
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.no_transition, R.anim.push_down)
    }
}
