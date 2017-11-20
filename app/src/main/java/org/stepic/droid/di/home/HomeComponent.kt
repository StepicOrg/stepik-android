package org.stepic.droid.di.home

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.HomeFragment

@HomeScope
@Subcomponent
interface HomeComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): HomeComponent
    }

    fun inject(homeFragment: HomeFragment)
}