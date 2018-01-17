package org.stepic.droid.adaptive.ui.activities

import android.support.v4.app.Fragment
import org.stepic.droid.adaptive.ui.fragments.RecommendationsFragment
import org.stepic.droid.base.SingleFragmentActivity

class AdaptiveCourseActivity : SingleFragmentActivity() {
    override fun createFragment() = RecommendationsFragment()
}