package org.stepic.droid.di.step

import dagger.Subcomponent
import org.stepic.droid.base.StepBaseFragment
import org.stepic.droid.di.comment.CommentsComponent
import org.stepic.droid.di.streak.StreakModule
import org.stepic.droid.ui.fragments.StepAttemptFragment
import org.stepik.android.view.injection.feedback.FeedbackDataModule

@StepScope
@Subcomponent(modules = arrayOf(StreakModule::class, CommentCountModule::class, FeedbackDataModule::class))
interface StepComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): StepComponent
    }

    fun commentsComponentBuilder(): CommentsComponent.Builder

    fun inject(stepFragment: StepBaseFragment)

    fun inject(stepAttemptFragment: StepAttemptFragment)
}
