package org.stepic.droid.features.achievements.ui.activity

import android.view.MenuItem
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.features.achievements.ui.fragments.AchievementsListFragment

class AchievementsListActivity: SingleFragmentActivity() {
    companion object {
        const val USER_ID_KEY = "user_id"
        const val IS_MY_PROFILE = "is_my_profile"
    }

    override fun createFragment() = AchievementsListFragment.newInstance(
            intent?.getLongExtra(USER_ID_KEY, 0) ?: 0,
            intent?.getBooleanExtra(IS_MY_PROFILE, false) ?: false
    )

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