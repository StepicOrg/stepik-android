package org.stepic.droid.features.achievements.ui.activity

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.features.achievements.ui.fragments.AchievementsListFragment

class AchievementsListActivity: SingleFragmentActivity() {
    companion object {
        private const val EXTRA_USER_ID = "user_id"
        private const val EXTRA_IS_MY_PROFILE = "is_my_profile"

        fun createIntent(context: Context, userId: Long, isMyProfile: Boolean): Intent =
                Intent(context, AchievementsListActivity::class.java).apply {
                    putExtra(EXTRA_USER_ID, userId)
                    putExtra(EXTRA_IS_MY_PROFILE, isMyProfile)
                }
    }

    override fun createFragment() = AchievementsListFragment.newInstance(
            intent?.getLongExtra(EXTRA_USER_ID, 0) ?: 0,
            intent?.getBooleanExtra(EXTRA_IS_MY_PROFILE, false) ?: false
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