package org.stepik.android.view.step_quiz.resolver

import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.presentation.step_quiz.StepQuizView

object StepQuizFormResolver {
    fun isQuizEnabled(state: StepQuizView.State.AttemptLoaded): Boolean =
        state.submissionState is StepQuizView.SubmissionState.Empty ||
        state.submissionState is StepQuizView.SubmissionState.Loaded &&
        state.submissionState.submission.status == Submission.Status.LOCAL

    fun isQuizActionEnabled(state: StepQuizView.State.AttemptLoaded): Boolean =
        isQuizEnabled(state) ||
        isSubmissionInTerminalState(state) &&
        with(state.restrictions) { maxSubmissionCount < 0 || maxSubmissionCount > submissionCount }

    fun isQuizRetryEnabled(state: StepQuizView.State.AttemptLoaded): Boolean =
        isQuizActionEnabled(state) &&
        state.submissionState is StepQuizView.SubmissionState.Loaded &&
        state.submissionState.submission.status == Submission.Status.CORRECT

    fun canMoveToNextStep(step: Step, lessonData: LessonData, state: StepQuizView.State.AttemptLoaded): Boolean =
        (state.submissionState as? StepQuizView.SubmissionState.Loaded)?.submission?.status == Submission.Status.CORRECT &&
        step.position < lessonData.lesson.steps.size

    fun canOnlyRetry(step: Step, lessonData: LessonData, state: StepQuizView.State.AttemptLoaded): Boolean =
        isSubmissionInTerminalState(state) &&
        !canMoveToNextStep(step, lessonData, state) &&
        with(state.restrictions) { maxSubmissionCount < 0 || maxSubmissionCount > submissionCount }

    fun isSubmissionInTerminalState(state: StepQuizView.State.AttemptLoaded): Boolean =
        state.submissionState is StepQuizView.SubmissionState.Loaded &&
        state.submissionState.submission.status.let { it == Submission.Status.CORRECT || it == Submission.Status.WRONG }
}