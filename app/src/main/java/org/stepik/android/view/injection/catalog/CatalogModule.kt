package org.stepik.android.view.injection.catalog

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.catalog.CatalogPresenter
import org.stepik.android.presentation.catalog.FiltersPresenter

@Module
abstract class CatalogModule {
    @Binds
    @IntoMap
    @ViewModelKey(CatalogPresenter::class)
    internal abstract fun bindCatalogPresenter(catalogPresenter: CatalogPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FiltersPresenter::class)
    internal abstract fun bindFiltersPresenter(filtersPresenter: FiltersPresenter): ViewModel
}