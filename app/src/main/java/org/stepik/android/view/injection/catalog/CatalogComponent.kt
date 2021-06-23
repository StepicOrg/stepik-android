package org.stepik.android.view.injection.catalog

import dagger.Subcomponent
import org.stepik.android.view.catalog.ui.fragment.CatalogFragment
import org.stepik.android.view.injection.course_recommendations.CourseRecommendationsDataModule
import org.stepik.android.view.injection.last_step.LastStepDataModule
import org.stepik.android.view.injection.wishlist.WishlistDataModule

@Subcomponent(modules = [
    CatalogBlockPresentationModule::class,
    CatalogDataModule::class,
    LastStepDataModule::class,
    CourseRecommendationsDataModule::class,
    WishlistDataModule::class
])
interface CatalogComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CatalogComponent
    }

    fun inject(catalogFragment: CatalogFragment)
}