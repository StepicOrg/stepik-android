package org.stepik.android.presentation.step

import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.review_instruction.model.ReviewInstructionData
import org.stepik.android.domain.step.model.StepNavigationDirection
import org.stepik.android.model.comments.DiscussionThread
import org.stepik.android.view.step.model.StepNavigationAction
import org.stepik.android.view.step.model.SectionUnavailableData

interface StepView {
    sealed class State {
        object Idle : State()
        data class Loaded(
            val stepWrapper: StepPersistentWrapper,
            val lessonData: LessonData,
            val discussionThreads: List<DiscussionThread>
        ) : State()
    }

    fun setState(state: State)

    fun setBlockingLoading(isLoading: Boolean)

    fun setNavigation(directions: Set<StepNavigationDirection>)
    fun showQuizReloadMessage()
    fun openShowSubmissionsWithReview(reviewInstructionData: ReviewInstructionData)
    fun handleNavigationAction(stepNavigationAction: StepNavigationAction)
    fun showSectionUnavailableMessage(sectionUnavailableData: SectionUnavailableData)
}