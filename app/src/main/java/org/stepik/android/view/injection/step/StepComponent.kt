package org.stepik.android.view.injection.step

import dagger.Subcomponent
import org.stepik.android.view.injection.attempt.AttemptDataModule
import org.stepik.android.view.injection.step_content.StepContentModule
import org.stepik.android.view.injection.step_quiz.StepQuizModule
import org.stepik.android.view.injection.step_quiz_text.TextStepQuizModule
import org.stepik.android.view.injection.submission.SubmissionDataModule
import org.stepik.android.view.step.ui.fragment.StepFragment
import org.stepik.android.view.step_quiz_text.ui.fragment.TextStepQuizFragment

@Subcomponent(modules = [
    StepModule::class,
    StepContentModule::class,
    StepQuizModule::class,

    TextStepQuizModule::class,
    AttemptDataModule::class,
    SubmissionDataModule::class
])
interface StepComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): StepComponent
    }

    fun inject(stepFragment: StepFragment)

    fun inject(textStepQuizFragment: TextStepQuizFragment)
}