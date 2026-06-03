package org.stepik.android.view.step_quiz.ui.factory

import android.view.View
import org.stepic.droid.databinding.FragmentStepQuizBinding
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class StepQuizViewStateDelegateFactory
@Inject
constructor() {
    fun create(quizFragmentLayout: View, vararg quizViews: View): ViewStateDelegate<StepQuizFeature.State> {
        val binding = FragmentStepQuizBinding.bind(quizFragmentLayout)
        return ViewStateDelegate<StepQuizFeature.State>()
            .apply {
                addState<StepQuizFeature.State.Idle>()
                addState<StepQuizFeature.State.Loading>(binding.stepQuizProgress)
                addState<StepQuizFeature.State.AttemptLoading>(binding.stepQuizProgress)
                addState<StepQuizFeature.State.AttemptLoaded>(
                    binding.stepQuizReviewTeacherMessage,
                    binding.stepQuizDiscountingPolicy,
                    binding.stepQuizFeedbackBlocks.root,
                    binding.stepQuizDescription,
                    binding.stepQuizActionContainer,
                    *quizViews
                )
                addState<StepQuizFeature.State.NetworkError>(binding.stepQuizNetworkError.root)
            }
    }
}
