package org.stepik.android.view.injection.in_app_web_view

import dagger.Subcomponent
import org.stepik.android.view.in_app_web_view.InAppWebViewDialogFragment

@Subcomponent(modules = [InAppWebViewModule::class])
interface InAppWebViewComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): InAppWebViewComponent
    }

    fun inject(inAppWebViewDialogFragment: InAppWebViewDialogFragment)
}