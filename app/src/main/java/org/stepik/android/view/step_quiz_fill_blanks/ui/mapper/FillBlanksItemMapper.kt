package org.stepik.android.view.step_quiz_fill_blanks.ui.mapper

import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.model.feedback.BlanksFeedback
import org.stepik.android.view.step_quiz_fill_blanks.ui.model.FillBlanksItem

class FillBlanksItemMapper {
    companion object {
        private const val TEXT = "text"
        private const val INPUT = "input"
        private const val SELECT = "select"

        private const val BREAK_TAG = "<br>"
    }
    fun mapToFillBlanksItems(attempt: Attempt, submission: Submission?, isEnabled: Boolean): List<FillBlanksItem> {
        var index = 0
        var blanksCounter = 0
        return attempt
            .dataset
            ?.components
            ?.map { component ->
                when (component.type) {
                    TEXT -> {
                        val result = generateTextComponents(index, component.text ?: "", component.options ?: emptyList(), isEnabled)
                        index = result.first
                        result.second
                    }

                    INPUT -> {
                        val blankIndex = blanksCounter++
                        listOf(FillBlanksItem.Input(index++, submission?.reply?.blanks?.getOrNull(blankIndex) ?: component.text ?: "", component.options ?: emptyList(), isEnabled, mapCorrect(blankIndex, submission)))
                    }

                    SELECT -> {
                        val blankIndex = blanksCounter++
                        listOf(FillBlanksItem.Select(index++, submission?.reply?.blanks?.getOrNull(blankIndex) ?: component.text ?: "", component.options ?: emptyList(), isEnabled, mapCorrect(blankIndex, submission)))
                    }

                    else ->
                        throw IllegalArgumentException("Component type not supported")
                }
            }
            ?.flatten()
            ?: emptyList()
    }

    private fun generateTextComponents(currentIndex: Int, componentText: String, componentOptions: List<String>, isEnabled: Boolean): Pair<Int, List<FillBlanksItem.Text>> {
        val textItems = componentText.split(BREAK_TAG)
        val components =  textItems.mapIndexed { index, text ->
            FillBlanksItem.Text(
                id = currentIndex + index,
                text = text,
                options = componentOptions,
                isEnabled = isEnabled,
                isWrapBefore = index > 0 // We add app:layout_wrapBefore="true" after each <br> tag
            )
        }
        val nextIndex = currentIndex + components.size
        return nextIndex to components
    }

    private fun mapCorrect(index: Int, submission: Submission?): Boolean? =
        (submission?.feedback as? BlanksFeedback)
            ?.blanksFeedback
            ?.getOrNull(index)
            ?: when (submission?.status) {
                Submission.Status.CORRECT ->
                    true
                Submission.Status.WRONG ->
                    false
                else ->
                    null
            }
}