package org.stepik.android.presentation.lesson

import org.stepik.android.domain.feedback.model.SupportEmailData
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.lesson.model.StepItem
import org.stepik.android.model.Step

interface LessonView {
    sealed class State {
        object Idle : State()
        object Loading : State()

        object LessonNotFound : State()
        object EmptyLogin : State()
        object NetworkError : State()

        data class LessonLoaded(
            val lessonData: LessonData,
            val stepsState: StepsState
        ) : State()
    }

    sealed class StepsState {
        object Idle : StepsState()
        object Loading : StepsState()
        object NetworkError : StepsState()
        object EmptySteps : StepsState()
        object AccessDenied : StepsState()
        class Loaded(
            val stepItems: List<StepItem>
        ) : StepsState()
    }

    fun setState(state: State)

    /**
     * Show step at [position]. [position] starts with 0.
     */
    fun showStepAtPosition(position: Int)

    fun showLessonInfoTooltip(stepWorth: Long, lessonTimeToComplete: Long, certificateThreshold: Long)

    fun showComments(step: Step, discussionId: Long)

    fun showRateDialog()

    fun sendTextFeedback(supportEmailData: SupportEmailData)
}