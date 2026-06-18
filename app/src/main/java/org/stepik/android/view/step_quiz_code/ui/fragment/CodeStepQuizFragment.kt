package org.stepik.android.view.step_quiz_code.ui.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.stepic.droid.databinding.LayoutStepQuizCodeBinding
import org.stepic.droid.ui.dialogs.ChangeCodeLanguageDialog
import org.stepic.droid.ui.dialogs.ProgrammingLanguageChooserDialogFragment
import org.stepik.android.domain.code_preference.model.InitCodePreference
import org.stepik.android.model.code.CodeOptions
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz.ui.fragment.DefaultStepQuizFragment
import org.stepik.android.view.step_quiz_code.ui.delegate.CodeLayoutDelegate
import org.stepik.android.view.step_quiz_code.ui.delegate.CodeQuizInstructionDelegate
import org.stepik.android.view.step_quiz_code.ui.delegate.CodeStepQuizFormDelegate
import org.stepik.android.view.step_quiz_fullscreen_code.ui.dialog.CodeStepQuizFullScreenDialogFragment
import ru.nobird.app.presentation.redux.container.ReduxView
import ru.nobird.android.view.base.ui.extension.showIfNotExists

class CodeStepQuizFragment :
    DefaultStepQuizFragment(),
    ReduxView<StepQuizFeature.State, StepQuizFeature.Action.ViewAction>,
    ChangeCodeLanguageDialog.Callback,
    ProgrammingLanguageChooserDialogFragment.Callback,
    CodeStepQuizFullScreenDialogFragment.Callback {
    companion object {
        fun newInstance(stepId: Long): Fragment =
            CodeStepQuizFragment()
                .apply {
                    this.stepId = stepId
                }
    }

    private lateinit var codeOptions: CodeOptions

    private lateinit var codeStepQuizFormDelegate: CodeStepQuizFormDelegate

    private var _binding: LayoutStepQuizCodeBinding? = null
    private val binding get() = _binding!!

    override val quizViews: Array<View>
        get() = arrayOf(binding.root)

    override fun createStepView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return LayoutStepQuizCodeBinding.inflate(layoutInflater, parent, false).also {
            _binding = it
        }.root
    }

    override fun createStepQuizFormDelegate(): StepQuizFormDelegate {
        codeOptions = stepWrapper.step.block?.options ?: throw IllegalArgumentException("Code options shouldn't be null")

        codeStepQuizFormDelegate = CodeStepQuizFormDelegate(
            codeStepQuizBinding = binding,
            codeOptions = codeOptions,
            codeLayoutDelegate = CodeLayoutDelegate(
                codeLayoutBinding = binding,
                step = stepWrapper.step,
                codeTemplates = codeOptions.codeTemplates,
                codeQuizInstructionDelegate = CodeQuizInstructionDelegate(binding, true),
                codeToolbarAdapter = null,
                onChangeLanguageClicked = ::onChangeLanguageClicked
            ),
            onFullscreenClicked = ::onFullScreenClicked,
            syncCodePreference = { language -> onSyncCodePreference(language) },
            onQuizChanged = ::syncReplyState
        )

        return codeStepQuizFormDelegate
    }

    override fun onChangeLanguage() {
        val languages = stepWrapper.step.block?.options?.limits?.keys?.sorted()?.toTypedArray() ?: emptyArray()

        val dialog = ProgrammingLanguageChooserDialogFragment.newInstance(languages)
        if (!dialog.isAdded) {
            dialog.show(childFragmentManager, null)
        }
    }

    override fun onLanguageChosen(programmingLanguage: String) {
        codeStepQuizFormDelegate.onLanguageSelected(programmingLanguage)
    }

    override fun onSyncCodeStateWithParent(lang: String, code: String, onSubmitClicked: Boolean) {
        codeStepQuizFormDelegate.updateCodeLayoutFromDialog(lang, code)
        if (onSubmitClicked) {
            onActionButtonClicked()
        }
    }

    override fun onSyncCodePreference(lang: String) {
        viewModel.onNewMessage(StepQuizFeature.Message.CreateCodePreference(
            languagesKey = codeOptions.codeTemplates.keys,
            language = lang,
            initCodePreference = InitCodePreference(
                sourceStepId = stepId,
                language = lang,
                codeTemplates = codeOptions.codeTemplates
            )
        ))
    }

    private fun onChangeLanguageClicked() {
        val dialog = ChangeCodeLanguageDialog.newInstance()
        if (!dialog.isAdded) {
            dialog.show(childFragmentManager, null)
        }
    }

    private fun onFullScreenClicked(lang: String, code: String) {
        CodeStepQuizFullScreenDialogFragment
            .newInstance(lang, code, codeOptions.codeTemplates, stepWrapper, lessonData.lesson.title.orEmpty())
            .showIfNotExists(childFragmentManager, CodeStepQuizFullScreenDialogFragment.TAG)
    }
}
