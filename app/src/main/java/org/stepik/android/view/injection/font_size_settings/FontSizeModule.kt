package org.stepik.android.view.injection.font_size_settings

import android.arch.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.font_size_settings.FontSizePresenter

@Module
abstract class FontSizeModule {
    @Binds
    @IntoMap
    @ViewModelKey(FontSizePresenter::class)
    internal abstract fun bindFontSizePresenter(fontSizePresenter: FontSizePresenter): ViewModel
}