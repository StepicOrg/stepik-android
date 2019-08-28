package org.stepik.android.view.step_quiz_sql.ui.delegate

import android.view.View
import kotlinx.android.synthetic.main.layout_step_quiz_code.view.*
import org.stepic.droid.R
import org.stepic.droid.model.code.extensionForLanguage
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.model.Reply
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.step_quiz.resolver.StepQuizFormResolver
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz_code.model.CodeStepQuizFormState
import org.stepik.android.view.ui.delegate.ViewStateDelegate

class SqlStepQuizFormDelegate(
    containerView: View,
    private val stepWrapper: StepPersistentWrapper
) : StepQuizFormDelegate {
    companion object {
        private const val SQL_LANG = "sql"
    }
    private var state: CodeStepQuizFormState = CodeStepQuizFormState.Idle
        set(value) {
            field = value

            viewStateDelegate.switchState(value)

            when (value) {
                is CodeStepQuizFormState.Lang -> {
                    codeLayout.setText(value.code)
                    codeLayout.lang = extensionForLanguage(value.lang)
                }
            }
        }

    private val viewStateDelegate = ViewStateDelegate<CodeStepQuizFormState>()

    private val stepQuizActions = containerView.stepQuizActions
    private val codeLayout = containerView.codeStepLayout

    init {
        viewStateDelegate.addState<CodeStepQuizFormState.Idle>()
        viewStateDelegate.addState<CodeStepQuizFormState.Lang>(codeLayout, stepQuizActions)
    }

    override fun createReply(): ReplyResult {
        val state = state
        return if (state is CodeStepQuizFormState.Lang) {
            ReplyResult.Success(Reply(solveSql = codeLayout.text.toString()))
        } else {
            ReplyResult.Error(codeLayout.context.getString(R.string.step_quiz_code_empty_lang))
        }
    }

    override fun setState(state: StepQuizView.State.AttemptLoaded) {
        val submission = (state.submissionState as? StepQuizView.SubmissionState.Loaded)
            ?.submission
        this.state = CodeStepQuizFormState.Lang(SQL_LANG, submission?.reply?.solveSql ?: "")

        val isEnabled = StepQuizFormResolver.isQuizEnabled(state)
        // codeLayout.isEnabled = isEnabled
    }


}