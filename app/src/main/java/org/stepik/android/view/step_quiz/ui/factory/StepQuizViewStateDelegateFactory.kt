package org.stepik.android.view.step_quiz.ui.factory

import android.view.View
import kotlinx.android.synthetic.main.fragment_step_quiz.view.*
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class StepQuizViewStateDelegateFactory
@Inject
constructor() {
    fun create(quizFragmentLayout: View, vararg quizViews: View): ViewStateDelegate<StepQuizFeature.State> =
        ViewStateDelegate<StepQuizFeature.State>()
            .apply {
                addState<StepQuizFeature.State.Idle>()
                addState<StepQuizFeature.State.Loading>(quizFragmentLayout.stepQuizProgress)
                addState<StepQuizFeature.State.AttemptLoading>(quizFragmentLayout.stepQuizProgress)
                addState<StepQuizFeature.State.AttemptLoaded>(
                    quizFragmentLayout.stepQuizReviewTeacherMessage,
                    quizFragmentLayout.stepQuizDiscountingPolicy,
                    quizFragmentLayout.stepQuizFeedbackBlocks,
                    quizFragmentLayout.stepQuizDescription,
                    quizFragmentLayout.stepQuizActionContainer,
                    *quizViews
                )
                addState<StepQuizFeature.State.NetworkError>(quizFragmentLayout.stepQuizNetworkError)
            }
}
