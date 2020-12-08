package org.stepik.android.view.injection.catalog_block

import dagger.Subcomponent
import org.stepik.android.view.catalog_block.ui.fragment.CatalogBlockFragment
import org.stepik.android.view.injection.last_step.LastStepDataModule
import org.stepik.android.view.injection.profile.ProfileDataModule

@Subcomponent(modules = [
    CatalogBlockPresentationModule::class,
    CatalogBlockDataModule::class,
    ProfileDataModule::class,
    LastStepDataModule::class
])
interface CatalogBlockComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CatalogBlockComponent
    }

    fun inject(catalogBlockFragment: CatalogBlockFragment)
}