package org.stepik.android.view.step_quiz_sorting.ui.model

import ru.nobird.android.core.model.Identifiable

data class SortingOption(
    override val id: Int,
    val option: String
) : Identifiable<Int>