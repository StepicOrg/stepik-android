package org.stepik.android.view.injection.magic_links

import dagger.Subcomponent
import org.stepik.android.view.magic_links.ui.dialog.MagicLinkDialogFragment

@Subcomponent(modules = [
    MagicLinksModule::class,
    MagicLinksDataModule::class
])
interface MagicLinksComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): MagicLinksComponent
    }

    fun inject(magicLinkDialogFragment: MagicLinkDialogFragment)
}