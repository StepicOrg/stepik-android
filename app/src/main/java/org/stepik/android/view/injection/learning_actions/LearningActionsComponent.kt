package org.stepik.android.view.injection.learning_actions

import dagger.Subcomponent
import org.stepik.android.view.injection.course_reviews.CourseReviewsDataModule
import org.stepik.android.view.injection.user_courses.UserCoursesDataModule
import org.stepik.android.view.injection.user_reviews.LearningActionsScope
import org.stepik.android.view.injection.user_reviews.UserReviewsPresentationModule
import org.stepik.android.view.injection.wishlist.WishlistDataModule
import org.stepik.android.view.learning_actions.ui.fragment.LearningActionsFragment
import org.stepik.android.view.user_reviews.ui.fragment.UserReviewsFragment

@LearningActionsScope
@Subcomponent(modules = [
    UserReviewsPresentationModule::class,
    WishlistDataModule::class,
    UserCoursesDataModule::class,
    CourseReviewsDataModule::class
])
interface LearningActionsComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): LearningActionsComponent
    }

    fun inject(learningActionsFragment: LearningActionsFragment)
    fun inject(userReviewsFragment: UserReviewsFragment)
}