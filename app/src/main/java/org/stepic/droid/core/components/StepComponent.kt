package org.stepic.droid.core.components

import dagger.Subcomponent
import org.stepic.droid.base.StepBaseFragment
import org.stepic.droid.core.PerFragment
import org.stepic.droid.core.modules.StepModule
import org.stepic.droid.ui.adapters.StepFragmentAdapter
import org.stepic.droid.ui.fragments.StepAttemptFragment
import org.stepic.droid.ui.fragments.StepsFragment
import org.stepic.droid.ui.fragments.VideoStepFragment

@PerFragment
@Subcomponent(modules = arrayOf(StepModule::class))
interface StepComponent {
    fun inject(stepFragment: StepBaseFragment)

    fun inject(stepsFragment: StepsFragment)

    fun inject(videoStepFragment: VideoStepFragment)

    fun inject(stepFragmentAdapter: StepFragmentAdapter)

    fun inject(stepAttemptFragment: StepAttemptFragment)
}
