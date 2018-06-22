package org.stepic.droid.features.achievements.ui.activity

import android.view.MenuItem
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.features.achievements.ui.fragments.AchievementsListFragment

class AchievementsListActivity: SingleFragmentActivity() {
    override fun createFragment() = AchievementsListFragment
            .newInstance(intent?.getLongExtra(AchievementsListFragment.USER_ID_KEY, 0) ?: 0)

    override fun applyTransitionPrev() {
        //no-op
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}