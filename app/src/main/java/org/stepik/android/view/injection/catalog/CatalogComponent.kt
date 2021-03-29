package org.stepik.android.view.injection.catalog

import dagger.Subcomponent
import org.stepik.android.view.catalog.ui.fragment.CatalogFragment
import org.stepik.android.view.injection.last_step.LastStepDataModule
import org.stepik.android.view.injection.profile.ProfileDataModule

@Subcomponent(modules = [
    CatalogBlockPresentationModule::class,
    CatalogDataModule::class,
    ProfileDataModule::class,
    LastStepDataModule::class
    // TODO APPS-3254 Add to dependency graph when you will be implementing UI
    // CourseRecommendationsDataModule::class
])
interface CatalogComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CatalogComponent
    }

    fun inject(catalogFragment: CatalogFragment)
}