package org.stepik.android.view.injection.learning_actions

import dagger.Subcomponent
import org.stepik.android.view.injection.user_courses.UserCoursesDataModule
import org.stepik.android.view.injection.wishlist.WishlistDataModule
import org.stepik.android.view.learning_actions.ui.fragment.LearningActionsFragment

@Subcomponent(modules = [
    LearningActionsPresentationModule::class,
    WishlistDataModule::class,
    UserCoursesDataModule::class
])
interface LearningActionsComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): LearningActionsComponent
    }

    fun inject(learningActionsFragment: LearningActionsFragment)
}