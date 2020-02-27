package org.stepic.droid.di.catalog

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.CatalogFragment
import org.stepik.android.view.injection.course.CourseDataModule
import org.stepik.android.view.injection.tags.TagsDataModule

@CatalogScope
@Subcomponent(modules = [CatalogModule::class, TagsDataModule::class, CourseDataModule::class])
interface CatalogComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): CatalogComponent
    }

    fun inject(catalogFragment: CatalogFragment)
}
