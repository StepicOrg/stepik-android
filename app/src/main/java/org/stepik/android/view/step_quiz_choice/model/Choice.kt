package org.stepik.android.view.step_quiz_choice.model

import ru.nobird.app.core.model.Identifiable

data class Choice(
    override val id: Int,
    val option: String,
    val correct: Boolean? = null,
    val feedback: String? = null,
    val isEnabled: Boolean = false
) : Identifiable<Int>
