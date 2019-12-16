package org.stepik.android.view.step_quiz_code.ui.fragment

import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.layout_step_quiz_code.*
import org.stepic.droid.R
import org.stepic.droid.ui.dialogs.ChangeCodeLanguageDialog
import org.stepic.droid.ui.dialogs.ProgrammingLanguageChooserDialogFragment
import org.stepik.android.model.code.CodeOptions
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz.ui.fragment.DefaultStepQuizFragment
import org.stepik.android.view.step_quiz_code.ui.delegate.CodeLayoutDelegate
import org.stepik.android.view.step_quiz_code.ui.delegate.CodeQuizInstructionDelegate
import org.stepik.android.view.step_quiz_code.ui.delegate.CodeStepQuizFormDelegate
import org.stepik.android.view.step_quiz_fullscreen_code.ui.dialog.CodeStepQuizFullScreenDialogFragment
import ru.nobird.android.view.base.ui.extension.showIfNotExists

class CodeStepQuizFragment :
    DefaultStepQuizFragment(),
    StepQuizView,
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

    override val quizLayoutRes: Int =
        R.layout.layout_step_quiz_code

    override val quizViews: Array<View>
        get() = arrayOf(stepQuizCodeContainer)

    override fun createStepQuizFormDelegate(view: View): StepQuizFormDelegate {
        codeOptions = stepWrapper.step.block?.options ?: throw IllegalArgumentException("Code options shouldn't be null")

        codeStepQuizFormDelegate = CodeStepQuizFormDelegate(
            containerView = view,
            codeOptions = codeOptions,
            codeLayoutDelegate = CodeLayoutDelegate(
                codeContainerView = view,
                step = stepWrapper.step,
                codeTemplates = codeOptions.codeTemplates,
                codeQuizInstructionDelegate = CodeQuizInstructionDelegate(view, true),
                codeToolbarAdapter = null,
                onChangeLanguageClicked = ::onChangeLanguageClicked
            ),
            onFullscreenClicked = ::onFullScreenClicked
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

    private fun onChangeLanguageClicked() {
        val dialog = ChangeCodeLanguageDialog.newInstance()
        if (!dialog.isAdded) {
            dialog.show(childFragmentManager, null)
        }
    }

    private fun onFullScreenClicked(lang: String, code: String) {
        val dialog = CodeStepQuizFullScreenDialogFragment.newInstance(lang, code, codeOptions.codeTemplates, stepWrapper, lessonData.lesson.title.orEmpty())
        dialog.setTargetFragment(this, CodeStepQuizFullScreenDialogFragment.CODE_PLAYGROUND_REQUEST)
        dialog.showIfNotExists(requireFragmentManager(), CodeStepQuizFullScreenDialogFragment.TAG)
    }
}