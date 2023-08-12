package org.stepik.android.view.injection.debug

import dagger.Subcomponent

@Subcomponent
interface DebugComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): DebugComponent
    }
}