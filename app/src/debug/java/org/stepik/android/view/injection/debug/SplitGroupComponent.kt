package org.stepik.android.view.injection.debug

import dagger.Subcomponent
import org.stepik.android.view.debug.ui.dialog.SplitGroupsDialogFragment

@Subcomponent(modules = [
    SplitGroupPresentationModule::class
])
interface SplitGroupComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): SplitGroupComponent
    }

    fun inject(splitGroupsDialogFragment: SplitGroupsDialogFragment)
}