package org.stepic.droid.adaptive.ui.activities

import android.view.MenuItem
import org.stepic.droid.adaptive.ui.fragments.AdaptiveOnboardingFragment
import org.stepic.droid.base.SingleFragmentActivity


class AdaptiveOnboardingActivity: SingleFragmentActivity() {
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        sharedPreferenceHelper.afterAdaptiveOnboardingPassed()
        super.onBackPressed()
    }

    override fun applyTransitionPrev() {
        //no-op
    }

    override fun createFragment() = AdaptiveOnboardingFragment()
}