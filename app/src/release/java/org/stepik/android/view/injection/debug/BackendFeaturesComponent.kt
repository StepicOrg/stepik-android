package org.stepik.android.view.injection.debug

import dagger.Subcomponent

@Subcomponent
interface BackendFeaturesComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): BackendFeaturesComponent
    }
}
