package org.stepik.android.view.injection.catalog

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.catalog.CatalogPresenter

@Module
abstract class CatalogModule {
    @Binds
    @IntoMap
    @ViewModelKey(CatalogPresenter::class)
    internal abstract fun bindCatalogPresenter(catalogPresenter: CatalogPresenter): ViewModel
}