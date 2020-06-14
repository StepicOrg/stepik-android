package org.stepik.android.view.step_quiz.resolver

import org.stepik.android.domain.step_quiz.model.StepQuizLessonData
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

    fun canMoveToNextStep(step: Step, stepQuizLessonData: StepQuizLessonData, state: StepQuizView.State.AttemptLoaded): Boolean =
        isQuizActionEnabled(state) &&
        isCorrect((state.submissionState as? StepQuizView.SubmissionState.Loaded)?.submission?.status) &&
        step.position < stepQuizLessonData.stepCount

    fun canOnlyRetry(step: Step, stepQuizLessonData: StepQuizLessonData, state: StepQuizView.State.AttemptLoaded): Boolean =
        isSubmissionInTerminalState(state) &&
        !canMoveToNextStep(step, stepQuizLessonData, state) &&
        with(state.restrictions) { maxSubmissionCount < 0 || maxSubmissionCount > submissionCount }

    fun isSubmissionInTerminalState(state: StepQuizView.State.AttemptLoaded): Boolean =
        state.submissionState is StepQuizView.SubmissionState.Loaded &&
        state.submissionState.submission.status.let { it == Submission.Status.CORRECT || it == Submission.Status.PARTIALLY_CORRECT || it == Submission.Status.WRONG }

    private fun isCorrect(submissionStatus: Submission.Status?) =
        submissionStatus == Submission.Status.PARTIALLY_CORRECT ||
        submissionStatus == Submission.Status.CORRECT
}