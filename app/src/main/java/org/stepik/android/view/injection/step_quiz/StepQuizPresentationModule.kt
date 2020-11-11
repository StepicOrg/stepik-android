package org.stepik.android.view.injection.step_quiz

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.stepik.android.presentation.base.injection.ViewModelKey
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.presentation.step_quiz.StepQuizViewModel
import org.stepik.android.presentation.step_quiz.dispatcher.StepQuizActionDispatcher
import org.stepik.android.presentation.step_quiz.reducer.StepQuizReducer
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewFeature
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewViewModel
import org.stepik.android.presentation.step_quiz_review.dispatcher.StepQuizReviewActionDispatcher
import org.stepik.android.presentation.step_quiz_review.reducer.StepQuizReviewReducer
import ru.nobird.android.core.model.safeCast
import ru.nobird.android.presentation.redux.container.wrapWithViewContainer
import ru.nobird.android.presentation.redux.dispatcher.tranform
import ru.nobird.android.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.android.presentation.redux.feature.ReduxFeature

@Module
object StepQuizPresentationModule {
    /**
     * Presentation
     */
    @Provides
    @IntoMap
    @ViewModelKey(StepQuizViewModel::class)
    internal fun provideStepQuizPresenter(
        stepQuizReducer: StepQuizReducer,
        stepQuizActionDispatcher: StepQuizActionDispatcher
    ): ViewModel =
        StepQuizViewModel(
            ReduxFeature(StepQuizFeature.State.Idle, stepQuizReducer)
                .wrapWithActionDispatcher(stepQuizActionDispatcher)
                .wrapWithViewContainer()
        )

    @Provides
    @IntoMap
    @ViewModelKey(StepQuizReviewViewModel::class)
    internal fun provideStepQuizReviewPresenter(
        stepQuizReviewReducer: StepQuizReviewReducer,
        stepQuizReviewActionDispatcher: StepQuizReviewActionDispatcher,
        stepQuizActionDispatcher: StepQuizActionDispatcher
    ): ViewModel =
        StepQuizReviewViewModel(
            ReduxFeature(StepQuizReviewFeature.State.Idle, stepQuizReviewReducer)
                .wrapWithActionDispatcher(stepQuizReviewActionDispatcher)
                .wrapWithActionDispatcher(
                    stepQuizActionDispatcher.tranform(
                        transformAction = { it.safeCast<StepQuizReviewFeature.Action.StepQuizAction>()?.action },
                        transformMessage = StepQuizReviewFeature.Message::StepQuizMessage
                    )
                )
                .wrapWithViewContainer()
        )
}