package org.stepik.android.view.injection.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.Multibinds
import org.stepik.android.presentation.base.injection.DaggerViewModelFactory

@Module
abstract class ViewModelFactoryModule {
    @Binds
    internal abstract fun bindViewModelFactory(daggerViewModelFactory: DaggerViewModelFactory): ViewModelProvider.Factory

    @Multibinds
    internal abstract fun bindViewModels(): Map<Class<out ViewModel>, @JvmSuppressWildcards ViewModel>
}