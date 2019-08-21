package org.stepik.android.view.step_quiz_matching.ui.mapper

import org.stepik.android.model.attempts.Attempt
import org.stepik.android.view.step_quiz_matching.ui.model.MatchingItem

class MatchingItemMapper {
    fun mapToMatchingItems(attempt: Attempt, isEnabled: Boolean): List<MatchingItem> =
        attempt
            .dataset
            ?.pairs
            ?.mapIndexed { index, pair ->
                listOf(
                    MatchingItem.Title(index, pair.first ?: "", isEnabled),
                    MatchingItem.Option(index, pair.second ?: "", isEnabled)
                )
            }
            ?.flatten()
            ?: emptyList()
}