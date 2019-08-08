package org.stepik.android.view.step_quiz_sorting.ui.delegate

import android.view.View
import kotlinx.android.synthetic.main.fragment_step_quiz.view.*
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate

class SortingStepQuizFormDelegate(
    containerView: View
) : StepQuizFormDelegate {
    private val context = containerView.context

    private val quizDescription = containerView.stepQuizDescription

    override fun setState(state: StepQuizView.State.AttemptLoaded) {

    }

    override fun createReply(): ReplyResult {
        
    }
}