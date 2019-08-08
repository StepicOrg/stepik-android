package org.stepik.android.view.step_quiz_sorting.ui.mapper

import org.stepik.android.model.attempts.Attempt
import org.stepik.android.view.step_quiz_sorting.ui.model.SortingOption

class SortingOptionMapper {
    fun mapToSortingOptions(attempt: Attempt): List<SortingOption> =
        attempt
            .dataset
            ?.options
            ?.mapIndexed { index, option -> SortingOption(index, option) }
            ?: emptyList()
}