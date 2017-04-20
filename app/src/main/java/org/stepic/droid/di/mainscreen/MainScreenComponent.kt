package org.stepic.droid.di.mainscreen

import dagger.Subcomponent
import org.stepic.droid.ui.activities.MainFeedActivity

@MainScreenScope
@Subcomponent(modules = arrayOf(MainScreenModule::class))
interface MainScreenComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): MainScreenComponent
    }

    fun inject(mainFeedActivity: MainFeedActivity)
}
