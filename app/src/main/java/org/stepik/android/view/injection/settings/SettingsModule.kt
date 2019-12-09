package org.stepik.android.view.injection.settings

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.settings.SettingsPresenter

@Module
abstract class SettingsModule {
    @Binds
    @IntoMap
    @ViewModelKey(SettingsPresenter::class)
    internal abstract fun bindSettingsPresenter(settingsPresenter: SettingsPresenter): ViewModel
}