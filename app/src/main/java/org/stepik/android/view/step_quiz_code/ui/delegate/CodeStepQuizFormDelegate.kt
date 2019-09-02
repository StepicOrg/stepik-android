package org.stepik.android.view.step_quiz_code.ui.delegate

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.layout_step_quiz_code.view.*
import org.stepic.droid.R
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.util.StepikAnimUtils
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.view.step_quiz.resolver.StepQuizFormResolver
import org.stepik.android.view.step_quiz_code.model.CodeStepQuizFormState
import org.stepik.android.view.step_quiz_code.ui.adapter.delegate.CodeLangAdapterDelegate
import ru.nobird.android.ui.adapterssupport.DefaultDelegateAdapter

class CodeStepQuizFormDelegate(
    containerView: View,
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
            val oldState = (state as? CodeStepQuizFormState.Lang)
                ?: return@setOnClickListener
            actionsListener.onFullscreenClicked(oldState.lang, oldState.code)
        }
    }

    override fun setState(state: StepQuizView.State.AttemptLoaded) {
        this.state = codeStepQuizFormStateMapper.mapToFormState(codeOptions, state)

        val isEnabled = StepQuizFormResolver.isQuizEnabled(state)
        codeLayout.isEnabled = isEnabled
        stepQuizActionChangeLang.isEnabled = isEnabled
    }

    fun updateCodeLayout(lang: String, code: String) {
        state = CodeStepQuizFormState.Lang(lang, code)
    }

    interface ActionsListener {
        fun onChangeLanguageClicked()
        fun onFullscreenClicked(lang: String, code: String)
    }
}