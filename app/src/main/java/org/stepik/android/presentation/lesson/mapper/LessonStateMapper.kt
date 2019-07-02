package org.stepik.android.presentation.lesson.mapper

import org.stepik.android.domain.lesson.model.StepItem
import org.stepik.android.model.Progress
import org.stepik.android.presentation.lesson.LessonView
import javax.inject.Inject

class LessonStateMapper
@Inject
constructor() {
    fun mergeStateWithProgress(state: LessonView.State, progress: Progress): LessonView.State =
        if (state !is LessonView.State.LessonLoaded ||
            state.stepsState !is LessonView.StepsState.Loaded ||
            state.stepsState.stepItems.all { it.stepWrapper.progress != progress.id && it.assignment?.progress != progress.id }) {
            state
        } else {
            state.copy(stepsState = LessonView.StepsState.Loaded(state.stepsState.stepItems.map { updateItemProgress(it, progress) }))
        }

    private fun updateItemProgress(stepItem: StepItem, progress: Progress): StepItem =
        when {
            stepItem.stepWrapper.progress == progress.id ->
                stepItem.copy(stepProgress = progress)

            stepItem.assignment?.progress == progress.id ->
                stepItem.copy(assignmentProgress = progress)

            else ->
                stepItem
        }
}