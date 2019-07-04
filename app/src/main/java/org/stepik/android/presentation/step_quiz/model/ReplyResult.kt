package org.stepik.android.presentation.step_quiz.model

import org.stepik.android.model.Reply

sealed class ReplyResult {
    data class Success(val reply: Reply): ReplyResult()
    data class Error(val message: String): ReplyResult()
}