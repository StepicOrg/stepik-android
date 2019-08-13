package org.stepik.android.view.step_quiz_code.ui.delegate

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.layout_step_quiz_code_fullscreen_instruction.view.*
import kotlinx.android.synthetic.main.layout_step_quiz_code_fullscreen_playground.view.*
import org.stepic.droid.R
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.custom.LatexSupportableEnhancedFrameLayout
import org.stepic.droid.ui.util.inflate
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepic.droid.ui.util.setOnKeyboardOpenListener
import org.stepic.droid.util.DpPixelsHelper
import org.stepik.android.view.step_quiz_code.model.CodeStepQuizFormState

class CodeStepQuizFullScreenFormDelegate(
    instructionContainerView: View,
    codeContainerView: View,
    keyboardExtensionContainer: ViewGroup?,
    stepWrapper: StepPersistentWrapper,
    actionsListener: ActionsListener
) : CodeQuizFormBaseDelegate(instructionContainerView, codeContainerView, stepWrapper) {

    // Flag is necessary, because keyboard listener is constantly invoked (probably global layout listener reacts to view changes)
    private var keyboardShown: Boolean = false
    private val codeSubmitButton = codeContainerView.codeSubmitButton
    private var latexLayout: LatexSupportableEnhancedFrameLayout? = null

    init {
        viewStateDelegate.addState<CodeStepQuizFormState.Idle>()
        viewStateDelegate.addState<CodeStepQuizFormState.Lang>(codeLayout)

        if (latexLayout == null) {
            latexLayout = instructionContainerView.stepQuizCodeTextContent as LatexSupportableEnhancedFrameLayout

            val text = stepWrapper
                .step
                .block
                ?.text
                ?.takeIf(String::isNotEmpty)

            if (text != null) {
                instructionContainerView.stepQuizCodeTextContent.setText(text)
                instructionContainerView.stepQuizCodeTextContent.setTextIsSelectable(true)
            }
        }
        setupCodeDetailContentData()
        instructionContainerView.stepQuizCodeDetailsContent.visibility = View.VISIBLE
        instructionContainerView.stepQuizCodeDetailsContent.isNestedScrollingEnabled = false
        /**
         * Keyboard extension
         */
        keyboardExtensionContainer.let { container ->
            val stepQuizCodeKeyboardExtension =
                container?.inflate(R.layout.layout_step_quiz_code_keyboard_extension) as RecyclerView
            stepQuizCodeKeyboardExtension.adapter = codeToolbarAdapter
            stepQuizCodeKeyboardExtension.layoutManager = LinearLayoutManager(container.context, LinearLayoutManager.HORIZONTAL, false)
            codeLayout.codeToolbarAdapter = codeToolbarAdapter

            container.addView(stepQuizCodeKeyboardExtension)
            stepQuizCodeKeyboardExtension.visibility = View.INVISIBLE // Apparently this fixes the offset bug when the current line is under the code toolbar adapter

            setOnKeyboardOpenListener(
                container,
                onKeyboardHidden = {
                    if (keyboardShown) {
                        stepQuizCodeKeyboardExtension.visibility = View.GONE
                        codeLayout.isNestedScrollingEnabled = true
                        codeLayout.layoutParams =
                            (codeLayout.layoutParams as RelativeLayout.LayoutParams)
                                .apply {
                                    bottomMargin = 0
                                }
                        codeLayout.setPadding(0, 0, 0, DpPixelsHelper.convertDpToPixel(80f).toInt())
                        codeSubmitButton.visibility = View.VISIBLE
                        keyboardShown = false
                    }
                },
                onKeyboardShown = {
                    if (!keyboardShown) {
                        stepQuizCodeKeyboardExtension.visibility = View.VISIBLE
                        codeLayout.isNestedScrollingEnabled = false
                        codeLayout.layoutParams =
                            (codeLayout.layoutParams as RelativeLayout.LayoutParams)
                                .apply {
                                    bottomMargin = stepQuizCodeKeyboardExtension.height
                                }
                        codeLayout.setPadding(0, 0, 0, 0)
                        codeSubmitButton.visibility = View.GONE
                        keyboardShown = true
                    }
                }
            )
        }
        stepQuizActionChangeLang.setCompoundDrawables(end = R.drawable.ic_arrow_bottom)
        stepQuizActionChangeLang.setOnClickListener { actionsListener.onChangeLanguageClicked() }
        codeSubmitButton.setOnClickListener { actionsListener.onSubmitClicked() }
    }

    interface ActionsListener {
        fun onChangeLanguageClicked()
        fun onSubmitClicked()
    }
}