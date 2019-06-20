package org.stepik.android.view.injection.step

import dagger.Subcomponent
import org.stepik.android.view.injection.step_content.StepContentModule
import org.stepik.android.view.injection.step_quiz.StepQuizModule
import org.stepik.android.view.step.ui.fragment.StepFragment

@Subcomponent(modules = [
    StepModule::class,
    StepContentModule::class,
    StepQuizModule::class
])
interface StepComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): StepComponent
    }

    fun inject(stepFragment: StepFragment)
}