package org.stepik.android.view.injection.step_quiz

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.step_quiz.StepQuizPresenter

@Module
abstract class StepQuizPresentationModule {
    /**
     * Presentation
     */
    @Binds
    @IntoMap
    @ViewModelKey(StepQuizPresenter::class)
    internal abstract fun bindStepQuizPresenter(stepQuizPresenter: StepQuizPresenter): ViewModel
}