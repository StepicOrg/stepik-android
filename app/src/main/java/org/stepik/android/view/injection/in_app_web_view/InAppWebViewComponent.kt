package org.stepik.android.view.injection.in_app_web_view

import dagger.Subcomponent
import org.stepik.android.view.in_app_web_view.InAppWebViewDialogFragment
import org.stepik.android.view.injection.magic_links.MagicLinksDataModule

@Subcomponent(modules = [
    InAppWebViewModule::class,
    MagicLinksDataModule::class
])
interface InAppWebViewComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): InAppWebViewComponent
    }

    fun inject(inAppWebViewDialogFragment: InAppWebViewDialogFragment)
}