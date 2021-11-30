package org.stepik.android.view.injection.debug

import dagger.Subcomponent
import org.stepik.android.view.debug.ui.activity.InAppPurchasesActivity

@Subcomponent(modules = [
    InAppPurchasesPresentationModule::class
])
interface InAppPurchasesComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): InAppPurchasesComponent
    }

    fun inject(inAppPurchasesActivity: InAppPurchasesActivity)
}