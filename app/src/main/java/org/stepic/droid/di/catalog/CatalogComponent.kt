package org.stepic.droid.di.catalog

import dagger.Subcomponent
import org.stepic.droid.ui.fragments.CatalogFragment
import org.stepik.android.view.injection.course_list.CourseListDataModule
import org.stepik.android.view.injection.tags.TagsDataModule

@CatalogScope
@Subcomponent(modules = [CatalogModule::class, TagsDataModule::class, CourseListDataModule::class])
interface CatalogComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): CatalogComponent
    }

    fun inject(catalogFragment: CatalogFragment)
}
