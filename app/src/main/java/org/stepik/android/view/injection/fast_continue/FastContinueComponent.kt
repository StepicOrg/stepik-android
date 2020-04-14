package org.stepik.android.view.injection.fast_continue

import dagger.Subcomponent
import org.stepik.android.view.fast_continue.ui.fragment.FastContinueFragment
import org.stepik.android.view.injection.last_step.LastStepDataModule

@FastContinueScope
@Subcomponent(modules = [FastContinueModule::class, LastStepDataModule::class])
interface FastContinueComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): FastContinueComponent
    }

    fun inject(fastContinueFragment: FastContinueFragment)
}