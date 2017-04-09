package org.stepic.droid.di.step

import dagger.Subcomponent
import org.stepic.droid.base.StepBaseFragment
import org.stepic.droid.di.streak.StreakModule
import org.stepic.droid.ui.fragments.StepAttemptFragment
import org.stepic.droid.ui.fragments.VideoStepFragment

@StepScope
@Subcomponent(modules = arrayOf(StreakModule::class))
interface StepComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): StepComponent
    }

    fun inject(stepFragment: StepBaseFragment)

    fun inject(videoStepFragment: VideoStepFragment)

    fun inject(stepAttemptFragment: StepAttemptFragment)
}
