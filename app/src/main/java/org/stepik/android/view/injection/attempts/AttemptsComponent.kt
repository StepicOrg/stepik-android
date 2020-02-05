package org.stepik.android.view.injection.attempts

import dagger.BindsInstance
import dagger.Subcomponent
import org.stepic.droid.di.qualifiers.CourseId
import org.stepik.android.view.attempts.ui.activity.SolutionsActivity
import org.stepik.android.view.injection.attempt.AttemptDataModule

@Subcomponent(modules = [AttemptsModule::class, AttemptDataModule::class])
interface AttemptsComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): AttemptsComponent

        @BindsInstance
        fun courseId(@CourseId courseId: Long): Builder
    }

    fun inject(solutionsActivity: SolutionsActivity)
}