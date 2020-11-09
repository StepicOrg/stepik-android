package org.stepik.android.view.step_quiz_sql.ui.delegate

import android.view.View
import kotlinx.android.synthetic.main.fragment_step_quiz.view.*
import kotlinx.android.synthetic.main.layout_step_quiz_code.view.*
import org.stepic.droid.R
import org.stepic.droid.model.code.ProgrammingLanguage
import org.stepik.android.model.Reply
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.step_quiz.resolver.StepQuizFormResolver
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate

class SqlStepQuizFormDelegate(
    containerView: View,
    private val onFullscreenClicked: (lang: String, code: String) -> Unit
) : StepQuizFormDelegate {

    private val quizDescription = containerView.stepQuizDescription

    private val codeLayout = containerView.codeStepLayout

    init {
        quizDescription.setText(R.string.step_quiz_sql_description)

        codeLayout.codeEditor.isFocusable = false
        codeLayout.codeEditor.setOnClickListener {
            onFullscreenClicked(ProgrammingLanguage.SQL.serverPrintableName, codeLayout.text.toString())
        }
    }

    override fun createReply(): ReplyResult =
        ReplyResult.Success(Reply(solveSql = codeLayout.text.toString()))

    override fun setState(state: StepQuizFeature.State.AttemptLoaded) {
        val reply = when (state.submissionState) {
            is StepQuizFeature.SubmissionState.Empty ->
                state.submissionState.reply

            is StepQuizFeature.SubmissionState.Loaded ->
                state.submissionState.submission.reply
        }
        codeLayout.setText(reply?.solveSql ?: "")
        codeLayout.lang = ProgrammingLanguage.SQL.serverPrintableName
        codeLayout.isEnabled = StepQuizFormResolver.isQuizEnabled(state)
    }

    fun updateCodeLayoutFromDialog(code: String) {
        codeLayout.setText(code)
    }
}