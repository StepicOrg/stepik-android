package org.stepic.droid.core

import dagger.Subcomponent
import org.stepic.droid.base.StepBaseFragment

@PerFragment
@Subcomponent(modules = arrayOf(StepModule::class))
interface StepComponent {
    fun inject(stepFragment: StepBaseFragment)
}
