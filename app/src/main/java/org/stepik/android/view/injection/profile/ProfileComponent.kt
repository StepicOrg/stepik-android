package org.stepik.android.view.injection.profile

import dagger.BindsInstance
import dagger.Subcomponent
import org.stepik.android.view.achievement.ui.fragment.AchievementsListFragment
import org.stepik.android.view.injection.user.UserDataModule
import org.stepik.android.view.injection.user_activity.UserActivityDataModule
import org.stepik.android.view.profile.ui.fragment.ProfileFragment
import org.stepik.android.view.profile.ui.fragment.ProfileFragmentOld
import org.stepik.android.view.profile_achievements.ui.fragment.ProfileAchievementsFragment
import org.stepik.android.view.profile_activities.ui.fragment.ProfileActivitiesFragment
import org.stepik.android.view.profile_detail.ui.fragment.ProfileDetailFragment

@ProfileScope
@Subcomponent(modules = [
    ProfileModuleOld::class,
    ProfileModule::class,
    ProfileDataModule::class,
    UserDataModule::class,
    UserActivityDataModule::class
])
interface ProfileComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): ProfileComponent

        @BindsInstance
        fun userId(@UserId userId: Long): Builder
    }

    fun inject(profileFragmentOld: ProfileFragmentOld)
    fun inject(profileFragment: ProfileFragment)
    fun inject(achievementsListFragment: AchievementsListFragment)
    fun inject(profileAchievementsFragment: ProfileAchievementsFragment)
    fun inject(profileDetailFragment: ProfileDetailFragment)
    fun inject(profileActivitiesFragment: ProfileActivitiesFragment)
}