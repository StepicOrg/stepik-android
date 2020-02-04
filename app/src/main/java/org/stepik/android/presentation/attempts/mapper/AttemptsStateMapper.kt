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

        val sectionIds = attemptItems
            .asSequence()
            .mapNotNull { item ->
                (item as? AttemptCacheItem.SubmissionItem)
                    ?.takeIf { it.submission.status == Submission.Status.LOCAL }
                    ?.section
                    ?.id
            }
            .toSet()

        val lessonIds = attemptItems
            .asSequence()
            .mapNotNull { item ->
                (item as? AttemptCacheItem.SubmissionItem)
                    ?.takeIf { it.submission.status == Submission.Status.LOCAL }
                    ?.lesson
                    ?.id
            }
            .toSet()

        var indexLeft = 0
        var indexRight = 0

        val result = ArrayList<AttemptCacheItem?>()

        while (indexLeft <= state.attempts.size && indexRight <= attemptItems.size) {
            when (compareAttemptCacheItems(state.attempts.getOrNull(indexLeft), attemptItems.getOrNull(indexRight))) {
                -1 -> {
                    result += state.attempts.getOrNull(indexLeft++)
                }
                0 -> {
                    result += attemptItems.getOrNull(indexRight++)
                    indexLeft++
                }
                1 -> {
                    val shouldAddItem =
                        when (val itemToAdd = attemptItems.getOrNull(indexRight)) {
                            is AttemptCacheItem.SectionItem ->
                                itemToAdd.section.id in sectionIds
                            is AttemptCacheItem.LessonItem ->
                                itemToAdd.lesson.id in lessonIds
                            is AttemptCacheItem.SubmissionItem ->
                                itemToAdd.submission.status == Submission.Status.LOCAL
                            else ->
                                false
                        }

                    if (shouldAddItem) {
                        result += attemptItems.getOrNull(indexRight++)
                    }
                        indexRight++
                }
            }
        }
        return state.copy(attempts = result.filterNotNull())
    }

    private fun compareAttemptCacheItems(a: AttemptCacheItem?, b: AttemptCacheItem?): Int {
        val (aSection, aUnit, aStep) = getAttemptCacheItemTriple(a)
        val (bSection, bUnit, bStep) = getAttemptCacheItemTriple(b)

        return (aSection?.position ?: -1).compareTo(bSection?.position ?: -1).takeIf { it != 0 }
            ?: (aUnit?.position ?: -1).compareTo(bUnit?.position ?: -1).takeIf { it != 0 }
            ?: (aStep?.position ?: -1).compareTo(bStep?.position ?: -1)
    }

    private fun getAttemptCacheItemTriple(item: AttemptCacheItem?) =
        when (item) {
            is AttemptCacheItem.SectionItem ->
                Triple(item.section, null, null)
            is AttemptCacheItem.LessonItem ->
                Triple(item.section, item.unit, null)
            is AttemptCacheItem.SubmissionItem ->
                Triple(item.section, item.unit, item.step)
            null ->
                Triple(null, null, null)
        }
}