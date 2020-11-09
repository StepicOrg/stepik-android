package org.stepik.android.view.step_quiz_code.mapper

import org.stepik.android.model.code.CodeOptions
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.view.step_quiz_code.model.CodeStepQuizFormState

class CodeStepQuizFormStateMapper {
    fun mapToFormState(codeOptions: CodeOptions, state: StepQuizFeature.State.AttemptLoaded): CodeStepQuizFormState {
        val submission = (state.submissionState as? StepQuizFeature.SubmissionState.Loaded)
            ?.submission

        val reply = submission?.reply
        val lang = reply?.language

        return when {
            lang != null ->
                CodeStepQuizFormState.Lang(lang, reply.code ?: "")

            codeOptions.codeTemplates.size == 1 ->
                codeOptions.codeTemplates.entries.first().let { (lang, code) -> CodeStepQuizFormState.Lang(lang, code) }

            else ->
                CodeStepQuizFormState.NoLang
        }
    }
}