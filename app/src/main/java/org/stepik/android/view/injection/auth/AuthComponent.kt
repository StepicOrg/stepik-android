package org.stepik.android.view.injection.auth

import dagger.Subcomponent
import org.stepik.android.view.auth.ui.activity.LoginActivity
import org.stepik.android.view.injection.profile.ProfileDataModule
import org.stepik.android.view.injection.user.UserDataModule
import org.stepik.android.view.injection.user_profile.UserProfileDataModule

@Subcomponent(
    modules = [
        AuthModule::class,
        UserDataModule::class,
        UserProfileDataModule::class,
        ProfileDataModule::class
    ]
)
interface AuthComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): AuthComponent
    }

    fun inject(loginActivity: LoginActivity)
}