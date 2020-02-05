package org.stepik.android.presentation.solutions.mapper

import org.stepic.droid.util.mutate
import org.stepik.android.domain.solutions.model.SolutionItem
import org.stepik.android.model.Submission
import org.stepik.android.presentation.solutions.SolutionsView
import javax.inject.Inject

class SolutionsStateMapper
@Inject
constructor() {
    fun setItemsEnabled(state: SolutionsView.State, isEnabled: Boolean): SolutionsView.State {
        if (state !is SolutionsView.State.AttemptsLoaded) {
            return state
        }
        val stateItems = state.attempts.map { solutionItem ->
            when (solutionItem) {
                is SolutionItem.SectionItem ->
                    solutionItem.copy(isEnabled = isEnabled)
                is SolutionItem.LessonItem ->
                    solutionItem.copy(isEnabled = isEnabled)
                is SolutionItem.SubmissionItem ->
                    solutionItem.copy(isEnabled = isEnabled)
            }
        }
        return SolutionsView.State.AttemptsLoaded(stateItems, !isEnabled)
    }

    fun mergeStateWithSubmission(state: SolutionsView.State, submission: Submission): SolutionsView.State {
        if (state !is SolutionsView.State.AttemptsLoaded) {
            return state
        }
        val itemIndex = state.attempts.indexOfFirst { it is SolutionItem.SubmissionItem && it.submission.attempt == submission.attempt }
        val stateItems = state.attempts.mutate {
            set(itemIndex, (get(itemIndex) as? SolutionItem.SubmissionItem)?.copy(submission = submission) ?: return@mutate)
        }
        return state.copy(attempts = stateItems)
    }

    fun mergeStateWithAttemptItems(state: SolutionsView.State, attemptItems: List<SolutionItem>): SolutionsView.State {
        if (state !is SolutionsView.State.AttemptsLoaded) {
            return state
        }

        val sectionIds = attemptItems
            .asSequence()
            .mapNotNull { item ->
                (item as? SolutionItem.SubmissionItem)
                    ?.takeIf { it.submission.status == Submission.Status.LOCAL }
                    ?.section
                    ?.id
            }
            .toSet()

        val lessonIds = attemptItems
            .asSequence()
            .mapNotNull { item ->
                (item as? SolutionItem.SubmissionItem)
                    ?.takeIf { it.submission.status == Submission.Status.LOCAL }
                    ?.lesson
                    ?.id
            }
            .toSet()

        var indexLeft = 0
        var indexRight = 0

        val result = ArrayList<SolutionItem?>()

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
                            is SolutionItem.SectionItem ->
                                itemToAdd.section.id in sectionIds
                            is SolutionItem.LessonItem ->
                                itemToAdd.lesson.id in lessonIds
                            is SolutionItem.SubmissionItem ->
                                itemToAdd.submission.status == Submission.Status.LOCAL
                            else ->
                                false
                        }

                    if (shouldAddItem) {
                        result += attemptItems.getOrNull(indexRight)
                    }
                    indexRight++
                }
            }
        }

        return state.copy(attempts = result.filterNotNull())
    }

    private fun compareAttemptCacheItems(a: SolutionItem?, b: SolutionItem?): Int {
        if (a == null) {
            return 1
        }
        val (aSection, aUnit, aStep) = getAttemptCacheItemTriple(a)
        val (bSection, bUnit, bStep) = getAttemptCacheItemTriple(b)

        return (aSection?.position ?: -1).compareTo(bSection?.position ?: -1).takeIf { it != 0 }
            ?: (aUnit?.position ?: -1).compareTo(bUnit?.position ?: -1).takeIf { it != 0 }
            ?: (aStep?.position ?: -1).compareTo(bStep?.position ?: -1)
    }

    private fun getAttemptCacheItemTriple(item: SolutionItem?) =
        when (item) {
            is SolutionItem.SectionItem ->
                Triple(item.section, null, null)
            is SolutionItem.LessonItem ->
                Triple(item.section, item.unit, null)
            is SolutionItem.SubmissionItem ->
                Triple(item.section, item.unit, item.step)
            null ->
                Triple(null, null, null)
        }
}