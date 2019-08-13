package org.stepik.android.view.step_quiz_code.ui.delegate

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.layout_step_quiz_code.view.*
import org.stepic.droid.R
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.util.StepikAnimUtils
import org.stepic.droid.ui.util.inflate
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepic.droid.ui.util.setOnKeyboardOpenListener
import org.stepik.android.view.base.ui.interfaces.KeyboardExtensionContainer
import org.stepik.android.view.step_quiz_code.model.CodeStepQuizFormState
import org.stepik.android.view.step_quiz_code.ui.adapter.delegate.CodeLangAdapterDelegate
import ru.nobird.android.ui.adapterssupport.DefaultDelegateAdapter

class CodeStepQuizFormDelegate(
    containerView: View,
    keyboardExtensionContainer: KeyboardExtensionContainer?,
    stepWrapper: StepPersistentWrapper,
    private val actionsListener: ActionsListener
) : CodeQuizFormBaseDelegate(containerView, containerView, stepWrapper) {

    private val stepQuizCodeLangChooserTitle = containerView.stepQuizCodeLangChooserTitle
    private val stepQuizCodeLangChooser = containerView.stepQuizCodeLangChooser
    private val stepQuizCodeLangChooserAdapter = DefaultDelegateAdapter<String>()

    init {
        viewStateDelegate.addState<CodeStepQuizFormState.Idle>()
        viewStateDelegate.addState<CodeStepQuizFormState.NoLang>(stepQuizCodeLangChooserTitle, stepQuizCodeLangChooser)
        viewStateDelegate.addState<CodeStepQuizFormState.Lang>(codeLayout, stepQuizActions)

        /**
         * Details
         */
        stepQuizCodeDetails.setOnClickListener {
            stepQuizCodeDetailsArrow.changeState()
            if (stepQuizCodeDetailsArrow.isExpanded()) {
                StepikAnimUtils.expand(stepQuizCodeDetailsContent)
            } else {
                StepikAnimUtils.collapse(stepQuizCodeDetailsContent)
            }
        }
        setupCodeDetailContentData()

        keyboardExtensionContainer
            ?.getKeyboardExtensionViewContainer()
            ?.let { container ->
                val stepQuizCodeKeyboardExtension =
                    container.inflate(R.layout.layout_step_quiz_code_keyboard_extension) as RecyclerView
                stepQuizCodeKeyboardExtension.adapter = codeToolbarAdapter
                stepQuizCodeKeyboardExtension.layoutManager = LinearLayoutManager(container.context, LinearLayoutManager.HORIZONTAL, false)
                codeLayout.codeToolbarAdapter = codeToolbarAdapter

                container.addView(stepQuizCodeKeyboardExtension)

                setOnKeyboardOpenListener(
                    container,
                    onKeyboardHidden = { stepQuizCodeKeyboardExtension.visibility = View.GONE },
                    onKeyboardShown = { stepQuizCodeKeyboardExtension.visibility = View.VISIBLE }
                )
            }

        /**
         * Lang chooser
         */
        stepQuizCodeLangChooserAdapter += CodeLangAdapterDelegate { state = CodeStepQuizFormState.Lang(it, codeOptions.codeTemplates[it] ?: "") }
        stepQuizCodeLangChooserAdapter.items =
            codeOptions.codeTemplates.keys.toList().sorted()

        stepQuizCodeLangChooserTitle.setCompoundDrawables(start = R.drawable.ic_step_quiz_code_lang)
        with(stepQuizCodeLangChooser) {
            layoutManager = LinearLayoutManager(context)
            adapter = stepQuizCodeLangChooserAdapter
        }

        /**
         * Actions
         */
        stepQuizActionChangeLang.setCompoundDrawables(end = R.drawable.ic_arrow_bottom)
        stepQuizActionChangeLang.setOnClickListener { actionsListener.onChangeLanguageClicked() }

        codeLayout.codeEditor.isFocusable = false
        codeLayout.codeEditor.setOnClickListener {
            if (state !is CodeStepQuizFormState.Lang) return@setOnClickListener
            actionsListener.onFullscreenClicked()
        }
    }

    interface ActionsListener {
        fun onChangeLanguageClicked()
        fun onFullscreenClicked()
    }
}