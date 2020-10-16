package org.stepik.android.view.injection.catalog

import dagger.Subcomponent
import org.stepik.android.view.catalog.ui.fragment.CatalogFragment
import org.stepik.android.view.injection.course_collection.CourseCollectionDataModule
import org.stepik.android.view.injection.course_list.CourseListScope
import org.stepik.android.view.injection.course_payments.CoursePaymentsDataModule
import org.stepik.android.view.injection.last_step.LastStepDataModule
import org.stepik.android.view.injection.profile.ProfileDataModule
import org.stepik.android.view.injection.tags.TagsDataModule

@CourseListScope
@Subcomponent(modules = [
    CatalogModule::class,
    CourseCollectionDataModule::class,
    CourseListCollectionModule::class,
    CoursePaymentsDataModule::class,
    LastStepDataModule::class,
    TagsDataModule::class,
    ProfileDataModule::class
])
interface CatalogComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CatalogComponent
    }

    fun inject(catalogFragment: CatalogFragment)
}