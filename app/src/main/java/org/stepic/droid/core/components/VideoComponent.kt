package org.stepic.droid.core.components

import dagger.Subcomponent
import org.stepic.droid.core.PerFragment
import org.stepic.droid.core.modules.StepModule
import org.stepic.droid.ui.fragments.VideoFragment

@PerFragment
@Subcomponent(modules = arrayOf(StepModule::class))
interface VideoComponent {
    fun inject(videoFragment: VideoFragment)
}
