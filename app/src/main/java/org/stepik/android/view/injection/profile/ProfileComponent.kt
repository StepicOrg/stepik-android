package org.stepik.android.view.injection.profile

import dagger.Subcomponent
import org.stepik.android.view.injection.user.UserDataModule
import org.stepik.android.view.injection.user_activity.UserActivityDataModule
import org.stepik.android.view.profile.ui.fragment.ProfileFragment
import org.stepik.android.view.profile.ui.fragment.ProfileFragmentOld

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
    }

    fun inject(profileFragmentOld: ProfileFragmentOld)
    fun inject(profileFragment: ProfileFragment)
}