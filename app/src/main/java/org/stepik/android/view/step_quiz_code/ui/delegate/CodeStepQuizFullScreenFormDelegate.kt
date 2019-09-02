package org.stepik.android.view.step_quiz_code.ui.delegate

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.activity_step_quiz_code_fullscreen.view.*
import kotlinx.android.synthetic.main.layout_step_quiz_code_fullscreen_instruction.view.*
import kotlinx.android.synthetic.main.layout_step_quiz_code_fullscreen_playground.view.*
import kotlinx.android.synthetic.main.view_step_quiz_submit_button.view.*
import org.stepic.droid.R
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.util.inflate
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepic.droid.ui.util.setOnKeyboardOpenListener
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.view.step_quiz_code.model.CodeStepQuizFormState

class CodeStepQuizFullScreenFormDelegate(
    instructionContainerView: View,
    codeContainerView: View,
    keyboardExtensionContainer: ViewGroup,
    stepWrapper: StepPersistentWrapper,
    actionsListener: ActionsListener
) : CodeQuizFormBaseDelegate(instructionContainerView, codeContainerView, stepWrapper) {

    // Flag is necessary, because keyboard listener is constantly invoked (probably global layout listener reacts to view changes)
    private var keyboardShown: Boolean = false
    private val submitButtonSeparator = codeContainerView.submitButtonSeparator
    private val codeSubmitButton = codeContainerView.codeSubmitButton
    private val retryButton = codeSubmitButton.stepQuizRetry
    private val fullScreenCodeToolbar = keyboardExtensionContainer.fullScreenCodeToolbar
    private val fullScreenCodeTabs = keyboardExtensionContainer.fullScreenCodeTabs
    private val fullScreenCodeSeparator = keyboardExtensionContainer.fullScreenCodeSeparator

    init {
        viewStateDelegate.addState<CodeStepQuizFormState.Idle>()
        viewStateDelegate.addState<CodeStepQuizFormState.Lang>(codeLayout)

        retryButton.visibility = View.GONE

        setupCodeDetailContentData()
        instructionContainerView.stepQuizCodeDetailsContent.visibility = View.VISIBLE
        instructionContainerView.stepQuizCodeDetailsContent.isNestedScrollingEnabled = false
        /**
         * Keyboard extension
         */
        keyboardExtensionContainer.let { container ->
            val stepQuizCodeKeyboardExtension =
                container.inflate(R.layout.layout_step_quiz_code_keyboard_extension) as RecyclerView
            stepQuizCodeKeyboardExtension.adapter = codeToolbarAdapter
            stepQuizCodeKeyboardExtension.layoutManager = LinearLayoutManager(container.context, LinearLayoutManager.HORIZONTAL, false)
            codeLayout.codeToolbarAdapter = codeToolbarAdapter

            container.addView(stepQuizCodeKeyboardExtension)
            stepQuizCodeKeyboardExtension.visibility = View.INVISIBLE // Apparently this fixes the offset bug when the current line is under the code toolbar adapter
            stepQuizCodeKeyboardExtension.layoutParams = (stepQuizCodeKeyboardExtension.layoutParams as RelativeLayout.LayoutParams)
                .apply {
                    addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                }

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
                        codeLayout.setPadding(0, 0, 0, container.context.resources.getDimensionPixelSize(R.dimen.step_quiz_fullscreen_code_layout_bottom_padding))
                        setViewsVisibility(View.VISIBLE)
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
                        setViewsVisibility(View.GONE)
                        keyboardShown = true
                    }
                }
            )
        }
        stepQuizActionChangeLang.setCompoundDrawables(end = R.drawable.ic_arrow_bottom)
        stepQuizActionChangeLang.setOnClickListener { actionsListener.onChangeLanguageClicked() }
        codeSubmitButton.setOnClickListener { actionsListener.onSubmitClicked() }
    }

    override fun setState(state: StepQuizView.State.AttemptLoaded) {
        this.state = codeStepQuizFormStateMapper.mapToFormState(codeOptions, state)
    }

    private fun setViewsVisibility(visibility: Int) {
        submitButtonSeparator.visibility = visibility
        codeSubmitButton.visibility = visibility
        fullScreenCodeToolbar.visibility = visibility
        fullScreenCodeTabs.visibility = visibility
        fullScreenCodeSeparator.visibility = visibility
    }

    interface ActionsListener {
        fun onChangeLanguageClicked()
        fun onSubmitClicked()
    }
}