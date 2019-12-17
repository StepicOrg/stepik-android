package org.stepik.android.view.step_quiz_sql.ui.fragment

import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.layout_step_quiz_code.*
import org.stepic.droid.R
import org.stepic.droid.model.code.ProgrammingLanguage
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz.ui.fragment.DefaultStepQuizFragment
import org.stepik.android.view.step_quiz_fullscreen_code.ui.dialog.CodeStepQuizFullScreenDialogFragment
import org.stepik.android.view.step_quiz_sql.ui.delegate.SqlStepQuizFormDelegate
import ru.nobird.android.view.base.ui.extension.showIfNotExists

class SqlStepQuizFragment : DefaultStepQuizFragment(), StepQuizView, CodeStepQuizFullScreenDialogFragment.Callback {
    companion object {
        fun newInstance(stepId: Long): Fragment =
            SqlStepQuizFragment()
                .apply {
                    this.stepId = stepId
                }
    }

    private lateinit var sqlStepQuizFormDelegate: SqlStepQuizFormDelegate

    override val quizLayoutRes: Int =
        R.layout.layout_step_quiz_sql

    override val quizViews: Array<View>
        get() = arrayOf(stepQuizCodeContainer)

    override fun createStepQuizFormDelegate(view: View): StepQuizFormDelegate {
        sqlStepQuizFormDelegate = SqlStepQuizFormDelegate(
            containerView = view,
            onFullscreenClicked = ::onFullScreenClicked
        )
        return sqlStepQuizFormDelegate
    }

    override fun onSyncCodeStateWithParent(lang: String, code: String, onSubmitClicked: Boolean) {
        sqlStepQuizFormDelegate.updateCodeLayoutFromDialog(code)
        if (onSubmitClicked) {
            onActionButtonClicked()
        }
    }

    private fun onFullScreenClicked(lang: String, code: String) {
        val dialog = CodeStepQuizFullScreenDialogFragment.newInstance(lang, code, mapOf(ProgrammingLanguage.SQL.serverPrintableName to ""), stepWrapper, lessonData.lesson.title.orEmpty())
        dialog.setTargetFragment(this, CodeStepQuizFullScreenDialogFragment.CODE_PLAYGROUND_REQUEST)
        dialog.showIfNotExists(requireFragmentManager(), CodeStepQuizFullScreenDialogFragment.TAG)
    }
}