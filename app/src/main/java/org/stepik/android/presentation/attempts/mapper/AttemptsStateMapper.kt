package org.stepik.android.presentation.attempts.mapper

import org.stepic.droid.util.mutate
import org.stepik.android.domain.attempts.model.AttemptCacheItem
import org.stepik.android.model.Submission
import org.stepik.android.presentation.attempts.AttemptsView
import javax.inject.Inject

class AttemptsStateMapper
@Inject
constructor() {
    fun setItemsEnabled(state: AttemptsView.State, isEnabled: Boolean): AttemptsView.State {
        if (state !is AttemptsView.State.AttemptsLoaded) {
            return state
        }
        val stateItems = state.attempts.map { attemptCacheItem ->
            when (attemptCacheItem) {
                is AttemptCacheItem.SectionItem ->
                    attemptCacheItem.copy(isEnabled = isEnabled)
                is AttemptCacheItem.LessonItem ->
                    attemptCacheItem.copy(isEnabled = isEnabled)
                is AttemptCacheItem.SubmissionItem ->
                    attemptCacheItem.copy(isEnabled = isEnabled)
            }
        }
        return AttemptsView.State.AttemptsLoaded(stateItems, !isEnabled)
    }

    fun mergeStateWithSubmission(state: AttemptsView.State, submission: Submission): AttemptsView.State {
        if (state !is AttemptsView.State.AttemptsLoaded) {
            return state
        }
        val itemIndex = state.attempts.indexOfFirst { it is AttemptCacheItem.SubmissionItem && it.submission.attempt == submission.attempt }
        val stateItems = state.attempts.mutate {
            set(itemIndex, (get(itemIndex) as? AttemptCacheItem.SubmissionItem)?.copy(submission = submission) ?: return@mutate)
        }
        return state.copy(attempts = stateItems)
    }

    fun mergeStateWithAttemptItems(state: AttemptsView.State, attemptItems: List<AttemptCacheItem>): AttemptsView.State {
        if (state !is AttemptsView.State.AttemptsLoaded) {
            return state
        }

        val newItems = attemptItems
            .asSequence()
            .filterIsInstance<AttemptCacheItem.SubmissionItem>()
            .associateBy { it.submission.attempt }

        val mergedItems = state.attempts.map { item ->
            if (item is AttemptCacheItem.SubmissionItem) {
                newItems[item.submission.attempt] ?: item
            } else {
                item
            }
        }

        return state.copy(attempts = mergedItems)
    }
}