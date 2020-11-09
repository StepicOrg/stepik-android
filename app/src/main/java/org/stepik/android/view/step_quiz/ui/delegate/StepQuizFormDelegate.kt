package org.stepik.android.view.step_quiz.ui.delegate

import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.presentation.step_quiz.model.ReplyResult

interface StepQuizFormDelegate {
    fun setState(state: StepQuizFeature.State.AttemptLoaded)

    /**
     * Generates reply from current form data
     */
    fun createReply(): ReplyResult
}