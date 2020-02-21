package org.stepik.android.view.injection.auth

import dagger.Subcomponent
import org.stepik.android.view.auth.ui.activity.LaunchActivity
import org.stepik.android.view.injection.user.UserDataModule
import org.stepik.android.view.injection.user_profile.UserProfileDataModule

@Subcomponent(
    modules = [
        UserDataModule::class,
        UserProfileDataModule::class
    ]
)
interface AuthComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): AuthComponent
    }

    fun inject(launchActivity: LaunchActivity)
}