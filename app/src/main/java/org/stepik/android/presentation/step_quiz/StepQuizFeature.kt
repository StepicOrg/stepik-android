package org.stepik.android.presentation.step_quiz

import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step_quiz.model.StepQuizRestrictions
import org.stepik.android.model.Reply
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt

interface StepQuizFeature {
    sealed class State {
        object Idle : State()
        object Loading : State()
        data class AttemptLoading(
            val restrictions: StepQuizRestrictions
        ) : State()
        data class AttemptLoaded(
            val attempt: Attempt,
            val submissionState: SubmissionState,
            val restrictions: StepQuizRestrictions
        ) : State()

        object NetworkError : State()
    }

    sealed class SubmissionState {
        data class Empty(val reply: Reply? = null) : SubmissionState()
        data class Loaded(val submission: Submission) : SubmissionState()
    }

    sealed class Message {
        data class InitWithStep(val stepWrapper: StepPersistentWrapper, val lessonData: LessonData, val forceUpdate: Boolean = false) : Message()
        data class FetchAttemptSuccess(
            val attempt: Attempt,
            val submissionState: SubmissionState,
            val restrictions: StepQuizRestrictions
        ) : Message()
        object FetchAttemptError : Message()

        data class CreateAttemptClicked(val step: Step) : Message()
        data class CreateAttemptSuccess(val attempt: Attempt, val submissionState: SubmissionState) : Message()
        object CreateAttemptError : Message()

        data class CreateSubmissionClicked(val step: Step, val reply: Reply) : Message()
        data class CreateSubmissionSuccess(val submission: Submission) : Message()
        object CreateSubmissionError : Message()

        data class SyncReply(val reply: Reply) : Message()
    }

    sealed class Action {
        data class FetchAttempt(val stepWrapper: StepPersistentWrapper, val lessonData: LessonData) : Action()

        data class CreateAttempt(val step: Step, val attempt: Attempt, val submissionState: SubmissionState) : Action()
        data class CreateSubmission(val step: Step, val attemptId: Long, val reply: Reply) : Action()
        data class SaveLocalSubmission(val submission: Submission) : Action()

        sealed class ViewAction : Action() {
            object ShowNetworkError : ViewAction() // error
        }
    }
}