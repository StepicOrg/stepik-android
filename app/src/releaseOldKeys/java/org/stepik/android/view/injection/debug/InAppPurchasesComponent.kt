package org.stepik.android.view.injection.debug

import dagger.Subcomponent

@Subcomponent
interface InAppPurchasesComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): InAppPurchasesComponent
    }
}