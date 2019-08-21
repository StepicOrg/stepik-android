package org.stepik.android.view.step_quiz_matching.ui.model

import ru.nobird.android.core.model.Identifiable

sealed class MatchingItem : Identifiable<Int> {
    data class Title(
        override val id: Int,
        val text: String,
        val isEnabled: Boolean
    ) : MatchingItem()

    data class Option(
        override val id: Int,
        val text: String,
        val isEnabled: Boolean
    ) : MatchingItem()
}