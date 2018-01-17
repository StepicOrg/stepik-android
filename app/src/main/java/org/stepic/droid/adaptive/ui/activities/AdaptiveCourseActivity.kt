package org.stepic.droid.adaptive.ui.activities

import org.stepic.droid.adaptive.ui.fragments.RecommendationsFragment
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.util.AppConstants

class AdaptiveCourseActivity : SingleFragmentActivity() {
    override fun createFragment() =
            RecommendationsFragment.newInstance(intent.getLongExtra(AppConstants.KEY_COURSE_LONG_ID, 0))
}