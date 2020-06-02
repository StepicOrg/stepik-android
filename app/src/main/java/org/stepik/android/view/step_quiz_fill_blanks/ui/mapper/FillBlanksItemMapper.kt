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
    }
    fun mapToFillBlanksItems(attempt: Attempt, submission: Submission?, isEnabled: Boolean): List<FillBlanksItem> {
        var blanksCounter = 0
        return attempt
            .dataset
            ?.components
            ?.mapIndexed { index, component ->
                when (component.type) {
                    TEXT ->
                        FillBlanksItem.Text(index, component.text ?: "", component.options ?: emptyList(), isEnabled)

                    INPUT -> {
                        val blankIndex = blanksCounter++
                        FillBlanksItem.Input(index, submission?.reply?.blanks?.getOrNull(blankIndex) ?: component.text ?: "", component.options ?: emptyList(), isEnabled, mapCorrect(blankIndex, submission))
                    }

                    SELECT -> {
                        val blankIndex = blanksCounter++
                        FillBlanksItem.Select(index, submission?.reply?.blanks?.getOrNull(blankIndex) ?: component.text ?: "", component.options ?: emptyList(), isEnabled, mapCorrect(blankIndex, submission))
                    }

                    else ->
                        throw IllegalArgumentException("Component type not supported")
                }
            }
            ?: emptyList()
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