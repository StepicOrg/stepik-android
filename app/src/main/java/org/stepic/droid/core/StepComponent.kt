package org.stepic.droid.core

import dagger.Subcomponent
import org.stepic.droid.base.StepBaseFragment
import org.stepic.droid.ui.fragments.StepsFragment
import org.stepic.droid.ui.fragments.VideoStepFragment

@PerFragment
@Subcomponent(modules = arrayOf(StepModule::class))
interface StepComponent {
    fun inject(stepFragment: StepBaseFragment)

    fun inject(stepsFragment: StepsFragment)

    fun inject(videoStepFragment: VideoStepFragment)
}
