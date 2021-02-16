package org.stepik.android.view.step_quiz_code.mapper

import org.stepik.android.model.Reply
import org.stepik.android.model.code.CodeOptions
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.view.step_quiz_code.model.CodeStepQuizFormState
import timber.log.Timber

class CodeStepQuizFormStateMapper {
    fun mapToFormState(codeOptions: CodeOptions, state: StepQuizFeature.State.AttemptLoaded): CodeStepQuizFormState {
        val (reply, lang) = resolveSubmissionState(state.submissionState)

        Timber.d("Submission state: ${state.submissionState}")

        return when {
            lang != null ->
                CodeStepQuizFormState.Lang(lang, reply?.code ?: "")

            codeOptions.codeTemplates.size == 1 ->
                codeOptions.codeTemplates.entries.first().let { (lang, code) -> CodeStepQuizFormState.Lang(lang, code) }

            else ->
                CodeStepQuizFormState.NoLang
        }
    }

    private fun resolveSubmissionState(submissionState: StepQuizFeature.SubmissionState): Pair<Reply?, String?> =
        when (submissionState) {
            is StepQuizFeature.SubmissionState.Empty ->
                submissionState.reply to submissionState.reply?.language
            is StepQuizFeature.SubmissionState.Loaded ->
                submissionState.submission.reply to submissionState.submission.reply?.language
            else ->
                throw IllegalArgumentException("Unsupported submission state = $submissionState")
        }
}