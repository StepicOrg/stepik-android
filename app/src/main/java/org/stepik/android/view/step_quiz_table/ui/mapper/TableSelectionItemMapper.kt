package org.stepik.android.view.step_quiz_table.ui.mapper

import org.stepik.android.model.Cell
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.view.step_quiz_table.ui.model.TableSelectionItem

class TableSelectionItemMapper {
    fun mapToTableSelectionItems(attempt: Attempt, submission: Submission?, isEnabled: Boolean): List<TableSelectionItem> =
        attempt
            .dataset
            ?.rows
            ?.mapIndexed { index, row ->
                TableSelectionItem(
                    index,
                    row,
                    submission
                        ?.reply
                        ?.tableChoices
                        ?.getOrNull(index)
                        ?.columns
                        ?: attempt.dataset?.columns?.map { Cell(id = it, answer = false) }
                        ?: emptyList(),
                    isEnabled = isEnabled
                )
            }
            ?: emptyList()
}