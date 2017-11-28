package org.stepic.droid.di.splash

import dagger.Subcomponent
import org.stepic.droid.ui.activities.SplashActivity

@SplashScope
@Subcomponent
interface SplashComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): SplashComponent
    }

    fun inject(splashActivity: SplashActivity)
}
