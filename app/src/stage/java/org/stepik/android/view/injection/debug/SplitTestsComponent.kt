package org.stepik.android.view.injection.debug

import dagger.Subcomponent

@Subcomponent
interface SplitTestsComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): SplitTestsComponent
    }
}