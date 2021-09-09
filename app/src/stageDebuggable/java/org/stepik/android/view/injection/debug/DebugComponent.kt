package org.stepik.android.view.injection.debug

import dagger.Subcomponent
import org.stepik.android.view.debug.ui.fragment.DebugFragment

@Subcomponent(modules = [
    DebugPresentationModule::class
])
interface DebugComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): DebugComponent
    }

    fun inject(debugFragment: DebugFragment)
}