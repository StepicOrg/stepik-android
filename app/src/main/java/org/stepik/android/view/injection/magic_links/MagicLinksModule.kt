package org.stepik.android.view.injection.magic_links

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.magic_links.MagicLinkPresenter

@Module
abstract class MagicLinksModule {
    /**
     * Presentation
     */
    @Binds
    @IntoMap
    @ViewModelKey(MagicLinkPresenter::class)
    internal abstract fun bindMagicLinkPresenter(magicLinkPresenter: MagicLinkPresenter): ViewModel
}