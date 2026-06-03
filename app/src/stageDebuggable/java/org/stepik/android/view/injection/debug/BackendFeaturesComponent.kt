package org.stepik.android.view.injection.debug

import dagger.Subcomponent
import org.stepik.android.view.debug.ui.dialog.BackendFeaturesDialogFragment

@Subcomponent(modules = [
    BackendFeaturesPresentationModule::class
])
interface BackendFeaturesComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): BackendFeaturesComponent
    }

    fun inject(backendFeaturesDialogFragment: BackendFeaturesDialogFragment)
}
