package org.stepic.droid.core.components

import dagger.Subcomponent
import org.stepic.droid.core.PerFragment
import org.stepic.droid.core.modules.MainFeedModule
import org.stepic.droid.ui.activities.MainFeedActivity

@PerFragment
@Subcomponent(modules = arrayOf(MainFeedModule::class))
interface MainFeedComponent {
    fun inject(mainFeedActivity: MainFeedActivity)
}
