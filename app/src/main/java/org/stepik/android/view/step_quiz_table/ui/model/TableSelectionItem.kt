package org.stepik.android.view.step_quiz_table.ui.model

import org.stepik.android.model.Cell
import ru.nobird.android.core.model.Identifiable

data class TableSelectionItem(
    override val id: Int,
    val titleText: String,
    val tableChoices: List<Cell>,
    val isEnabled: Boolean
) : Identifiable<Int>