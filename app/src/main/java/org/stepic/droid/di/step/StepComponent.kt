package org.stepic.droid.di.step

import dagger.Subcomponent
import org.stepic.droid.di.comment.CommentsComponent
import org.stepic.droid.di.streak.StreakModule
import org.stepik.android.view.injection.feedback.FeedbackDataModule

@StepScope
@Subcomponent(modules = [StreakModule::class, FeedbackDataModule::class])
interface StepComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): StepComponent
    }

    fun commentsComponentBuilder(): CommentsComponent.Builder
}
