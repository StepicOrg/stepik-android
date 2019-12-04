package org.stepik.android.view.injection.achievements

import dagger.Subcomponent
import org.stepik.android.view.achievement.ui.dialog.AchievementDetailsDialog
import org.stepik.android.view.achievement.ui.fragment.AchievementsListFragment

@Subcomponent(modules = [AchievementsModule::class])
interface AchievementsComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): AchievementsComponent
    }

    fun inject(achievementsListFragment: AchievementsListFragment)
    fun inject(achievementDetailsDialog: AchievementDetailsDialog)
}