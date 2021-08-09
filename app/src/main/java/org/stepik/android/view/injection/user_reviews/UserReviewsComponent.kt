package org.stepik.android.view.injection.user_reviews

import dagger.Subcomponent
import org.stepik.android.view.injection.course_reviews.CourseReviewsDataModule
import org.stepik.android.view.injection.wishlist.WishlistDataModule
import org.stepik.android.view.user_reviews.ui.fragment.UserReviewsFragment

@Subcomponent(modules = [
    UserReviewsPresentationModule::class,
    CourseReviewsDataModule::class,
    WishlistDataModule::class
])
interface UserReviewsComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): UserReviewsComponent
    }

    fun inject(userReviewsFragment: UserReviewsFragment)
}