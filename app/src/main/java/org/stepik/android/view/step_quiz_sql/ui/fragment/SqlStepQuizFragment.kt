package org.stepik.android.view.step_quiz_sql.ui.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.stepic.droid.databinding.LayoutStepQuizSqlBinding
import org.stepic.droid.model.code.ProgrammingLanguage
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz.ui.fragment.DefaultStepQuizFragment
import org.stepik.android.view.step_quiz_fullscreen_code.ui.dialog.CodeStepQuizFullScreenDialogFragment
import org.stepik.android.view.step_quiz_sql.ui.delegate.SqlStepQuizFormDelegate
import ru.nobird.app.presentation.redux.container.ReduxView
import ru.nobird.android.view.base.ui.extension.showIfNotExists

class SqlStepQuizFragment :
    DefaultStepQuizFragment(),
    ReduxView<StepQuizFeature.State, StepQuizFeature.Action.ViewAction>,
    CodeStepQuizFullScreenDialogFragment.Callback {
    companion object {
        fun newInstance(stepId: Long): Fragment =
            SqlStepQuizFragment()
                .apply {
                    this.stepId = stepId
                }
    }

    private lateinit var sqlStepQuizFormDelegate: SqlStepQuizFormDelegate

    private var _binding: LayoutStepQuizSqlBinding? = null
    private val binding: LayoutStepQuizSqlBinding
        get() = requireNotNull(_binding)

    override val quizViews: Array<View>
        get() = arrayOf(binding.root)

    override fun createStepView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return LayoutStepQuizSqlBinding.inflate(layoutInflater, parent, false).also {
            _binding = it
        }.root
    }

    override fun createStepQuizFormDelegate(): StepQuizFormDelegate {
        sqlStepQuizFormDelegate = SqlStepQuizFormDelegate(
            stepQuizBinding = stepQuizBinding,
            sqlStepQuizBinding = binding,
            onFullscreenClicked = ::onFullScreenClicked,
            onQuizChanged = ::syncReplyState
        )
        return sqlStepQuizFormDelegate
    }

    override fun onSyncCodeStateWithParent(lang: String, code: String, onSubmitClicked: Boolean) {
        sqlStepQuizFormDelegate.updateCodeLayoutFromDialog(code)
        if (onSubmitClicked) {
            onActionButtonClicked()
        }
    }

    override fun onSyncCodePreference(lang: String) {}

    private fun onFullScreenClicked(lang: String, code: String) {
        CodeStepQuizFullScreenDialogFragment
            .newInstance(lang, code, mapOf(ProgrammingLanguage.SQL.serverPrintableName to ""), stepWrapper, lessonData.lesson.title.orEmpty())
            .showIfNotExists(childFragmentManager, CodeStepQuizFullScreenDialogFragment.TAG)
    }
}
