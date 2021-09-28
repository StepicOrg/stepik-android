package org.stepik.android.view.injection.course_list.search_result

import dagger.Subcomponent
import org.stepik.android.view.course_list.ui.fragment.CourseListSearchFragment
import org.stepik.android.view.injection.course.CourseDataModule
import org.stepik.android.view.injection.course_payments.CoursePaymentsDataModule
import org.stepik.android.view.injection.last_step.LastStepDataModule
import org.stepik.android.view.injection.search.CatalogSearchSuggestionsPresentationModule
import org.stepik.android.view.injection.search.SearchDataModule
import org.stepik.android.view.injection.wishlist.WishlistDataModule

@CourseListSearchResultScope
@Subcomponent(modules = [
    CourseListSearchResultModule::class,
    CatalogSearchSuggestionsPresentationModule::class,
    SearchDataModule::class,
    CourseDataModule::class,
    CoursePaymentsDataModule::class,
    LastStepDataModule::class,
    WishlistDataModule::class
])
interface CourseListSearchResultComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseListSearchResultComponent
    }

    fun inject(courseListSearchFragment: CourseListSearchFragment)
}