package org.stepik.android.view.step_quiz.ui.delegate

import org.stepik.android.model.Reply
import org.stepik.android.presentation.step_quiz.StepQuizView

interface StepQuizFormDelegate {
    fun setState(state: StepQuizView.State)

    /**
     * Generates reply from current form data
     */
    fun createReply(): Reply

    /**
     * Validates form for ability to create a reply
     * @returns null if validation successful or message string otherwise
     */
    fun validateForm(): String?
}