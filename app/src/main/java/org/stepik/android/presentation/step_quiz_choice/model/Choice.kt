package org.stepik.android.presentation.step_quiz_choice.model

data class Choice(
    val option: String,
    var correct: Boolean? = null,
    var feedback: String? = null,
    var isEnabled: Boolean = false
)