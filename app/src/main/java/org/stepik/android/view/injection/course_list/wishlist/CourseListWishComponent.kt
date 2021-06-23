package org.stepik.android.view.injection.course_list.wishlist

import dagger.Subcomponent
import org.stepik.android.view.course_list.ui.fragment.CourseListWishFragment
import org.stepik.android.view.injection.course.CourseDataModule
import org.stepik.android.view.injection.course_payments.CoursePaymentsDataModule
import org.stepik.android.view.injection.last_step.LastStepDataModule
import org.stepik.android.view.injection.wishlist.WishlistDataModule

@CourseListWishScope
@Subcomponent(modules = [
    CourseListWishModule::class,
    WishlistDataModule::class,
    CourseDataModule::class,
    CoursePaymentsDataModule::class,
    LastStepDataModule::class
])
interface CourseListWishComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): CourseListWishComponent
    }

    fun inject(courseListWishFragment: CourseListWishFragment)
}