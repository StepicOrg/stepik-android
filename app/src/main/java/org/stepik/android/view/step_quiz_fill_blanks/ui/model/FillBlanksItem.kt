package org.stepik.android.view.step_quiz_fill_blanks.ui.model

import ru.nobird.android.core.model.Identifiable

sealed class FillBlanksItem : Identifiable<Int> {
    data class Text(
        override val id: Int,
        val text: String,
        val options: List<String>,
        val isEnabled: Boolean,
        val correct: Boolean? = null,
        val isWrapBefore: Boolean = false
    ) : FillBlanksItem()

    data class Input(
        override val id: Int,
        val text: String,
        val options: List<String>,
        val isEnabled: Boolean,
        val correct: Boolean? = null
    ) : FillBlanksItem()

    data class Select(
        override val id: Int,
        val text: String,
        val options: List<String>,
        val isEnabled: Boolean,
        val correct: Boolean? = null
    ) : FillBlanksItem()
}