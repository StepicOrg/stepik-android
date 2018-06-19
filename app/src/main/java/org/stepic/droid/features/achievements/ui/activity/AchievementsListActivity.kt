package org.stepic.droid.features.achievements.ui.activity

import org.stepic.droid.base.SingleFragmentActivity
import org.stepic.droid.features.achievements.ui.fragments.AchievementsListFragment

class AchievementsListActivity: SingleFragmentActivity() {
    override fun createFragment() = AchievementsListFragment
            .newInstance(intent?.getLongExtra(AchievementsListFragment.USER_ID_KEY, 0) ?: 0)
}