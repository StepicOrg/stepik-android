package org.stepic.droid.di.login

import dagger.Subcomponent
import org.stepik.android.view.auth.ui.activity.LaunchActivity
import org.stepik.android.view.auth.ui.activity.LoginActivity
import org.stepic.droid.ui.activities.RegisterActivity
import org.stepik.android.view.injection.profile.ProfileDataModule
import org.stepik.android.view.injection.user.UserDataModule
import org.stepik.android.view.injection.user_profile.UserProfileDataModule

@LoginScope
@Subcomponent(modules = [
    UserDataModule::class,
    UserProfileDataModule::class,
    ProfileDataModule::class
])
interface LoginComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): LoginComponent
    }

    fun inject(launchActivity: LaunchActivity)

    fun inject(registerActivity: RegisterActivity)

}
