package org.stepic.droid.di.login

import dagger.Subcomponent
import org.stepic.droid.ui.activities.LaunchActivity
import org.stepic.droid.ui.activities.LoginActivity
import org.stepic.droid.ui.activities.RegisterActivity

@LoginScope
@Subcomponent
interface LoginComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): LoginComponent
    }

    fun inject(launchActivity: LaunchActivity)

    fun inject(registerActivity: RegisterActivity)

    fun inject(loginActivity: LoginActivity)
}
