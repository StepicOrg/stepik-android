package org.stepik.android.view.step_quiz_sql.ui.delegate

import android.view.View
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import org.stepic.droid.R
import org.stepic.droid.code.ui.CodeEditorLayout
import org.stepic.droid.databinding.FragmentStepQuizBinding
import org.stepic.droid.databinding.LayoutStepQuizSqlBinding
import org.stepic.droid.model.code.ProgrammingLanguage
import org.stepik.android.model.Reply
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.step_quiz.resolver.StepQuizFormResolver
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate

class SqlStepQuizFormDelegate(
    private val quizDescription: TextView,
    private val codeLayout: CodeEditorLayout,
    private val onFullscreenClicked: (lang: String, code: String) -> Unit,
    private val onQuizChanged: (ReplyResult) -> Unit
) : StepQuizFormDelegate {
    constructor(
        stepQuizBinding: FragmentStepQuizBinding,
        sqlStepQuizBinding: LayoutStepQuizSqlBinding,
        onFullscreenClicked: (lang: String, code: String) -> Unit,
        onQuizChanged: (ReplyResult) -> Unit
    ) : this(
        stepQuizBinding.stepQuizDescription,
        sqlStepQuizBinding.codeStepLayout,
        onFullscreenClicked,
        onQuizChanged
    )

    constructor(
        containerView: View,
        onFullscreenClicked: (lang: String, code: String) -> Unit,
        onQuizChanged: (ReplyResult) -> Unit
    ) : this(
        containerView.findViewById(R.id.stepQuizDescription),
        containerView.findViewById(R.id.codeStepLayout),
        onFullscreenClicked,
        onQuizChanged
    )

    init {
        quizDescription.setText(R.string.step_quiz_sql_description)

        codeLayout.codeEditor.isFocusable = false
        codeLayout.codeEditor.setOnClickListener {
            onFullscreenClicked(ProgrammingLanguage.SQL.serverPrintableName, codeLayout.text.toString())
        }
        codeLayout.codeEditor.doAfterTextChanged { onQuizChanged(createReply()) }
    }

    override fun createReply(): ReplyResult =
        ReplyResult(Reply(solveSql = codeLayout.text.toString()), ReplyResult.Validation.Success)

    override fun setState(state: StepQuizFeature.State.AttemptLoaded) {
        val reply = when (state.submissionState) {
            is StepQuizFeature.SubmissionState.Empty ->
                state.submissionState.reply

            is StepQuizFeature.SubmissionState.Loaded ->
                state.submissionState.submission.reply
        }
        codeLayout.setTextIfChanged(reply?.solveSql ?: "")
        codeLayout.lang = ProgrammingLanguage.SQL.serverPrintableName
        codeLayout.isEnabled = StepQuizFormResolver.isQuizEnabled(state)
    }

    fun updateCodeLayoutFromDialog(code: String) {
        codeLayout.setText(code)
    }
}
