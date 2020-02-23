package org.stepik.android.view.injection.auth

import dagger.Subcomponent
import org.stepik.android.view.auth.ui.activity.SocialAuthActivity
import org.stepik.android.view.auth.ui.activity.CredentialAuthActivity
import org.stepik.android.view.auth.ui.activity.RegistrationActivity
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

    fun inject(credentialAuthActivity: CredentialAuthActivity)
    fun inject(registerActivity: SocialAuthActivity)
    fun inject(registrationActivity: RegistrationActivity)
}