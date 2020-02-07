package org.stepik.android.view.injection.solutions

import dagger.BindsInstance
import dagger.Subcomponent
import org.stepic.droid.di.qualifiers.CourseId
import org.stepik.android.view.injection.attempt.AttemptDataModule
import org.stepik.android.view.solutions.ui.activity.SolutionsActivity

@Subcomponent(modules = [SolutionsModule::class, AttemptDataModule::class])
interface SolutionsComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): SolutionsComponent

        @BindsInstance
        fun courseId(@CourseId courseId: Long): Builder
    }

    fun inject(solutionsActivity: SolutionsActivity)
}