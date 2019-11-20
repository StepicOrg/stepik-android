package org.stepik.android.view.injection.profile

import dagger.Subcomponent
import org.stepik.android.view.profile.ui.fragment.ProfileFragment

@Subcomponent(modules = [
    ProfileModule::class,
    ProfileDataModule::class
])
interface ProfileComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): ProfileComponent
    }

    fun inject(profileFragment: ProfileFragment)
}