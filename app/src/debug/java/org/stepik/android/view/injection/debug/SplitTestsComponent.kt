package org.stepik.android.view.injection.debug

import dagger.Subcomponent
import org.stepik.android.view.debug.ui.dialog.SplitTestsDialogFragment

@Subcomponent(
    modules = [
        SplitTestsPresentationModule::class
    ]
)
interface SplitTestsComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): SplitTestsComponent
    }

    fun inject(splitTestsDialogFragment: SplitTestsDialogFragment)
}