package org.stepik.android.view.step_quiz_code.model

sealed class CodeStepQuizFormState {
    object Idle : CodeStepQuizFormState()
    object NoLang : CodeStepQuizFormState()
    data class Lang(val lang: String, val code: String) : CodeStepQuizFormState()
}