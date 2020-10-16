package org.stepik.android.view.injection.step_quiz

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.step_quiz.StepQuizPresenter
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewPresenter

@Module
abstract class StepQuizPresentationModule {
    /**
     * Presentation
     */
    @Binds
    @IntoMap
    @ViewModelKey(StepQuizPresenter::class)
    internal abstract fun bindStepQuizPresenter(stepQuizPresenter: StepQuizPresenter): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StepQuizReviewPresenter::class)
    internal abstract fun bindStepQuizReviewPresenter(stepQuizReviewPresenter: StepQuizReviewPresenter): ViewModel
}