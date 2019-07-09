package org.stepik.android.view.step_quiz.ui.delegate

import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz.model.ReplyResult

interface StepQuizFormDelegate {
    fun setState(state: StepQuizView.State.AttemptLoaded)

    /**
     * Generates reply from current form data
     */
    fun createReply(): ReplyResult
}