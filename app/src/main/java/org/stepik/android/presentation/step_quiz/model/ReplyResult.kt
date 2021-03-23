package org.stepik.android.presentation.step_quiz.model

import org.stepik.android.model.Reply

data class ReplyResult(
    val reply: Reply,
    val validation: Validation
) {
    sealed class Validation {
        object Success : Validation()
        data class Error(val message: String) : Validation()
    }
}