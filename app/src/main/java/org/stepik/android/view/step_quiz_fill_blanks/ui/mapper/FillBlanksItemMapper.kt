package org.stepik.android.view.step_quiz_fill_blanks.ui.mapper

import org.stepik.android.model.attempts.Attempt
import org.stepik.android.view.step_quiz_fill_blanks.ui.model.FillBlanksItem

class FillBlanksItemMapper {
    companion object {
        private const val TEXT = "text"
        private const val INPUT = "input"
        private const val SELECT = "select"
    }
    fun mapToFillBlanksItems(attempt: Attempt, isEnabled: Boolean): List<FillBlanksItem> =
        attempt
            .dataset
            ?.components
            ?.mapIndexed { index, component ->
                when (component.type) {
                    TEXT ->
                        FillBlanksItem.Text(index, component.text ?: "", component.options ?: emptyList(), isEnabled)

                    INPUT ->
                        FillBlanksItem.Input(index, component.text ?: "", component.options ?: emptyList(), isEnabled)

                    SELECT ->
                        FillBlanksItem.Select(index, component.text ?: "", component.options ?: emptyList(), isEnabled)

                    else ->
                        throw IllegalArgumentException("Component type not supported")
                }
            }
            ?: emptyList()
}