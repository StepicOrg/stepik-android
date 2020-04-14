package org.stepik.android.view.injection.filter

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.filter.FiltersPresenter

@Module
abstract class FilterModule {
    @Binds
    @IntoMap
    @ViewModelKey(FiltersPresenter::class)
    internal abstract fun bindFiltersPresenter(filtersPresenter: FiltersPresenter): ViewModel
}