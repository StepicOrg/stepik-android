package org.stepic.droid.di.profile

import dagger.Subcomponent
import org.stepic.droid.di.streak.StreakModule
import org.stepic.droid.features.achievements.ui.fragments.AchievementsListFragment
import org.stepic.droid.ui.fragments.ProfileFragment


@ProfileScope
@Subcomponent(modules = [StreakModule::class, ProfileModule::class])
interface ProfileComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): ProfileComponent
    }

    fun inject(profileFragment: ProfileFragment)
    fun inject(achievementsListFragment: AchievementsListFragment)
}
