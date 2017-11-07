package org.stepic.droid.di.catalog

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.CatalogFragment

@CatalogScope
@Subcomponent
interface CatalogComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): CatalogComponent
    }

    fun inject(catalogFragment: CatalogFragment)
}
