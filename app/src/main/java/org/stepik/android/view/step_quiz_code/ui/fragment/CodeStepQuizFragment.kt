package org.stepik.android.view.step_quiz_code.ui.fragment

import android.support.v4.app.Fragment
import android.view.View
import kotlinx.android.synthetic.main.layout_step_quiz_code.*
import org.stepic.droid.R
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.dialogs.ChangeCodeLanguageDialog
import org.stepic.droid.ui.dialogs.ProgrammingLanguageChooserDialogFragment
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz.ui.fragment.DefaultStepQuizFragment
import org.stepik.android.view.step_quiz_code.ui.delegate.CodeQuizInstructionDelegate
import org.stepik.android.view.step_quiz_code.ui.delegate.CodeStepQuizFormDelegate
import org.stepik.android.view.step_quiz_code.ui.delegate.CoreCodeStepDelegate
import org.stepik.android.view.step_quiz_fullscreen_code.ui.dialog.CodeStepQuizFullScreenDialogFragment

class CodeStepQuizFragment : DefaultStepQuizFragment(), StepQuizView, ChangeCodeLanguageDialog.Callback, ProgrammingLanguageChooserDialogFragment.Callback, CodeStepQuizFullScreenDialogFragment.Callback {
    companion object {
        fun newInstance(stepPersistentWrapper: StepPersistentWrapper, lessonData: LessonData): Fragment =
            CodeStepQuizFragment()
                .apply {
                    this.stepWrapper = stepPersistentWrapper
                    this.lessonData = lessonData
                }
    }

    private lateinit var codeStepQuizFormDelegate: CodeStepQuizFormDelegate

    override val quizLayoutRes: Int =
        R.layout.layout_step_quiz_code

    override val quizViews: Array<View>
        get() = arrayOf(stepQuizCodeContainer)

    override fun createStepQuizFormDelegate(view: View): StepQuizFormDelegate {
        val actionsListener = object : CoreCodeStepDelegate.ActionsListener {
            override fun onChangeLanguageClicked() {
                val dialog = ChangeCodeLanguageDialog.newInstance()
                if (!dialog.isAdded) {
                    dialog.show(childFragmentManager, null)
                }
            }

            override fun onFullscreenClicked(lang: String, code: String) {
                val supportFragmentManager = fragmentManager
                    ?.takeIf { it.findFragmentByTag(CodeStepQuizFullScreenDialogFragment.TAG) == null }
                    ?: return

                val dialog = CodeStepQuizFullScreenDialogFragment.newInstance(lang, code, stepWrapper, lessonData)
                dialog.setTargetFragment(this@CodeStepQuizFragment, CodeStepQuizFullScreenDialogFragment.CODE_PLAYGROUND_REQUEST)
                dialog.show(supportFragmentManager, CodeStepQuizFullScreenDialogFragment.TAG)
            }
        }

        codeStepQuizFormDelegate = CodeStepQuizFormDelegate(
            containerView = view,
            coreCodeStepDelegate = CoreCodeStepDelegate(
                codeContainerView = view,
                stepWrapper = stepWrapper,
                codeQuizInstructionDelegate = CodeQuizInstructionDelegate(view, true),
                actionsListener = actionsListener,
                codeToolbarAdapter = null
            )
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
}