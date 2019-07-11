package org.stepik.android.presentation.step_quiz_choice.model

import ru.nobird.android.core.model.Identifiable

data class Choice(
    val option: String,
    val correct: Boolean? = null,
    val feedback: String? = null,
    val isEnabled: Boolean = false
) : Identifiable<String> {
    override val id: String
        get() = option
}