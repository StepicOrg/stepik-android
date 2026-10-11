package org.stepik.android.view.injection.filter_search

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.view.injection.base.ViewModelFactoryModule
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.filter_search.FilterSearchPresenter

@Module(includes = [ViewModelFactoryModule::class])
abstract class FilterSearchModule {
    @Binds
    @IntoMap
    @ViewModelKey(FilterSearchPresenter::class)
    internal abstract fun bindFiltersSearchPresenter(filterSearchPresenter: FilterSearchPresenter): ViewModel
}