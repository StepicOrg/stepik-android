package org.stepic.droid.di.login

import dagger.Subcomponent
import org.stepic.droid.ui.activities.LaunchActivity
import org.stepic.droid.ui.activities.LoginActivity
import org.stepic.droid.ui.activities.RegisterActivity
import org.stepik.android.view.injection.user.UserDataModule
import org.stepik.android.view.injection.user_profile.UserProfileDataModule

@LoginScope
@Subcomponent(modules = [
    UserDataModule::class,
    UserProfileDataModule::class
])
interface LoginComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): LoginComponent
    }

    fun inject(launchActivity: LaunchActivity)

    fun inject(registerActivity: RegisterActivity)

    fun inject(loginActivity: LoginActivity)
}
