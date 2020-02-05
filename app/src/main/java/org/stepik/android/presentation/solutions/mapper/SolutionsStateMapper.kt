package org.stepik.android.presentation.solutions.mapper

import org.stepic.droid.util.mutate
import org.stepik.android.domain.solutions.model.SolutionItem
import org.stepik.android.model.Submission
import org.stepik.android.presentation.solutions.SolutionsView
import javax.inject.Inject

class SolutionsStateMapper
@Inject
constructor() {
    fun setSolutionItemsEnabled(state: SolutionsView.State, isEnabled: Boolean): SolutionsView.State {
        if (state !is SolutionsView.State.SolutionsLoaded) {
            return state
        }
        val stateItems = state.solutions.map { solutionItem ->
            when (solutionItem) {
                is SolutionItem.SectionItem ->
                    solutionItem.copy(isEnabled = isEnabled)
                is SolutionItem.LessonItem ->
                    solutionItem.copy(isEnabled = isEnabled)
                is SolutionItem.SubmissionItem ->
                    solutionItem.copy(isEnabled = isEnabled)
                else ->
                    solutionItem
            }
        }
        return SolutionsView.State.SolutionsLoaded(stateItems, !isEnabled)
    }

    fun mergeStateWithSubmission(state: SolutionsView.State, submission: Submission): SolutionsView.State {
        if (state !is SolutionsView.State.SolutionsLoaded) {
            return state
        }
        val itemIndex = state.solutions.indexOfFirst { it is SolutionItem.SubmissionItem && it.submission.attempt == submission.attempt }
        val stateItems = state.solutions.mutate {
            set(itemIndex, (get(itemIndex) as? SolutionItem.SubmissionItem)?.copy(submission = submission) ?: return@mutate)
        }
        return state.copy(solutions = stateItems)
    }

    fun mergeStateWithSolutionItems(state: SolutionsView.State, solutionItems: List<SolutionItem>): SolutionsView.State {
        if (state !is SolutionsView.State.SolutionsLoaded) {
            return state
        }

        val sectionIds = solutionItems
            .asSequence()
            .mapNotNull { item ->
                (item as? SolutionItem.SubmissionItem)
                    ?.takeIf { it.submission.status == Submission.Status.LOCAL }
                    ?.section
                    ?.id
            }
            .toSet()

        val lessonIds = solutionItems
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

        while (indexLeft <= state.solutions.size && indexRight <= solutionItems.size) {
            when (compareSolutionItems(state.solutions.getOrNull(indexLeft), solutionItems.getOrNull(indexRight))) {
                -1 -> {
                    result += state.solutions.getOrNull(indexLeft++)
                }
                0 -> {
                    result += solutionItems.getOrNull(indexRight++)
                    indexLeft++
                }
                1 -> {
                    val shouldAddItem =
                        when (val itemToAdd = solutionItems.getOrNull(indexRight)) {
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
                        result += solutionItems.getOrNull(indexRight)
                    }
                    indexRight++
                }
            }
        }

        return state.copy(solutions = result.filterNotNull())
    }

    fun mapToSolutionsState(solutions: List<SolutionItem>): SolutionsView.State =
        if (solutions.isEmpty()) {
            SolutionsView.State.Empty
        } else {
            SolutionsView.State.SolutionsLoaded(solutions, isSending = false)
        }

    private fun compareSolutionItems(a: SolutionItem?, b: SolutionItem?): Int {
        if (a == null) {
            return 1
        }
        val (aSection, aUnit, aStep) = getSolutionItemTriple(a)
        val (bSection, bUnit, bStep) = getSolutionItemTriple(b)

        return (aSection?.position ?: -1).compareTo(bSection?.position ?: -1).takeIf { it != 0 }
            ?: (aUnit?.position ?: -1).compareTo(bUnit?.position ?: -1).takeIf { it != 0 }
            ?: (aStep?.position ?: -1).compareTo(bStep?.position ?: -1)
    }

    private fun getSolutionItemTriple(item: SolutionItem?) =
        when (item) {
            is SolutionItem.SectionItem ->
                Triple(item.section, null, null)
            is SolutionItem.LessonItem ->
                Triple(item.section, item.unit, null)
            is SolutionItem.SubmissionItem ->
                Triple(item.section, item.unit, item.step)
            else ->
                Triple(null, null, null)
        }
}