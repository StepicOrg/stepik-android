package org.stepic.droid.core.components

import dagger.Subcomponent
import org.stepic.droid.core.PerFragment
import org.stepic.droid.core.modules.LoginModule
import org.stepic.droid.ui.activities.LaunchActivity
import org.stepic.droid.ui.activities.LoginActivity
import org.stepic.droid.ui.activities.RegisterActivity

@PerFragment
@Subcomponent(modules = arrayOf(LoginModule::class))
interface LoginComponent {
    fun inject(launchActivity: LaunchActivity)

    fun inject(registerActivity: RegisterActivity)

    fun inject(loginActivity: LoginActivity)
}
