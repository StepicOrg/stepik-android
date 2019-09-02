package org.stepik.android.view.step_quiz.mapper

import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.TextUtil
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.view.step_quiz.model.StepQuizFeedbackState

class StepQuizFeedbackMapper {
    fun mapToStepQuizFeedbackState(step: Step, state: StepQuizView.State): StepQuizFeedbackState =
        if (state is StepQuizView.State.AttemptLoaded && state.submissionState is StepQuizView.SubmissionState.Loaded) {
            when (state.submissionState.submission.status) {
                Submission.Status.CORRECT ->
                    StepQuizFeedbackState.Correct(formatHint(step, state.submissionState.submission))

                Submission.Status.WRONG ->
                    StepQuizFeedbackState.Wrong(formatHint(step, state.submissionState.submission))

                Submission.Status.EVALUATION ->
                    StepQuizFeedbackState.Evaluation

                else ->
                    StepQuizFeedbackState.Idle
            }
        } else {
            StepQuizFeedbackState.Idle
        }

    private fun formatHint(step: Step, submission: Submission): String? =
        submission
            .hint
            ?.takeIf(String::isNotEmpty)
            ?.replace("\n", "<br />")
            ?.let {
                val showLaTeX = step.block?.name == AppConstants.TYPE_MATH // LaTeX support only for math feedback https://github.com/bioinf/edy/blob/dca133bc752ee0b21a2a419c1b7c6d5be3859a3e/apps/frontend/stepic/app/templates/components/submission-show.hbs#L48
                if (showLaTeX) {
                    it
                } else {
                    """<pre><span style="font-family: 'Roboto';">$it</span></pre>"""
                }
            }
            ?.let(TextUtil::linkify)
}