package org.stepic.droid.adaptive.ui.activities

import android.view.MenuItem
import org.stepic.droid.adaptive.ui.fragments.RecommendationsFragment
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.util.AppConstants

class AdaptiveCourseActivity : SingleFragmentActivity() {
    override fun createFragment() =
        RecommendationsFragment.newInstance(intent.getParcelableExtra(AppConstants.KEY_COURSE_BUNDLE))

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun applyTransitionPrev() {
        //no-op
    }
}