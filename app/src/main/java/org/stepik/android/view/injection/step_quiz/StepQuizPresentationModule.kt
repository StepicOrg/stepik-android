package org.stepik.android.view.injection.step_quiz

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.presentation.step_quiz.StepQuizPresenter
import org.stepik.android.presentation.step_quiz.dispatcher.StepQuizActionDispatcher
import org.stepik.android.presentation.step_quiz.reducer.StepQuizReducer
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewPresenter
import ru.nobird.android.presentation.redux.container.wrapWithViewContainer
import ru.nobird.android.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.android.presentation.redux.feature.ReduxFeature

@Module
abstract class StepQuizPresentationModule {
    /**
     * Presentation
     */

    @Binds
    @IntoMap
    @ViewModelKey(StepQuizReviewPresenter::class)
    internal abstract fun bindStepQuizReviewPresenter(stepQuizReviewPresenter: StepQuizReviewPresenter): ViewModel

    @Module
    companion object {
        @Provides
        @JvmStatic
        @IntoMap
        @ViewModelKey(StepQuizPresenter::class)
        internal fun provideStepQuizPresenter(
            stepQuizReducer: StepQuizReducer,
            stepQuizActionDispatcher: StepQuizActionDispatcher
        ): ViewModel =
            StepQuizPresenter(
                ReduxFeature(StepQuizFeature.State.Idle, stepQuizReducer)
                    .wrapWithActionDispatcher(stepQuizActionDispatcher)
                    .wrapWithViewContainer()
            )
    }
}