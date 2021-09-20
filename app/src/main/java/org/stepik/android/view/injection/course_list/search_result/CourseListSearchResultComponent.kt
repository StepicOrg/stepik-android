package org.stepik.android.view.injection.course_list.search_result

import dagger.BindsInstance
import dagger.Subcomponent
import org.stepic.droid.di.qualifiers.CourseId
import org.stepik.android.view.course_list.ui.fragment.CourseListSearchFragment
import org.stepik.android.view.injection.course.CourseDataModule
import org.stepik.android.view.injection.course_payments.CoursePaymentsDataModule
import org.stepik.android.view.injection.last_step.LastStepDataModule
import org.stepik.android.view.injection.wishlist.WishlistDataModule

@CourseListSearchResultScope
@Subcomponent(modules = [
    CourseListSearchResultModule::class,
    CourseDataModule::class,
    CoursePaymentsDataModule::class,
    LastStepDataModule::class,
    WishlistDataModule::class
])
interface CourseListSearchResultComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseListSearchResultComponent

        @BindsInstance
        fun courseId(@CourseId courseId: Long): Builder
    }

    fun inject(courseListSearchFragment: CourseListSearchFragment)
}