package org.stepic.droid.di.profile

import dagger.Subcomponent
import org.stepic.droid.di.streak.StreakModule
import org.stepic.droid.ui.fragments.ProfileFragment
import org.stepik.android.view.injection.user.UserDataModule
import org.stepik.android.view.injection.user_profile.UserProfileDataModule


@ProfileScope
@Subcomponent(modules = [StreakModule::class, ProfileModule::class, UserDataModule::class, UserProfileDataModule::class])
interface ProfileComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): ProfileComponent
    }

    fun inject(profileFragmentOld: ProfileFragment)
}
