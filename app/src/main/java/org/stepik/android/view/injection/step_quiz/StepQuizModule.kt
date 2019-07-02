package org.stepik.android.view.injection.step_quiz

import dagger.Binds
import dagger.Module
import org.stepik.android.view.step_quiz.ui.factory.StepQuizFragmentFactory
import org.stepik.android.view.step_quiz.ui.factory.StepQuizFragmentFactoryImpl

@Module
abstract class StepQuizModule {
    @Binds
    internal abstract fun bindStepQuizFragmentFactoryImpl(
        stepQuizFragmentFactoryImpl: StepQuizFragmentFactoryImpl
    ): StepQuizFragmentFactory
}