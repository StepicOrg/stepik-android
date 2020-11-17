package org.stepik.android.view.step_quiz_code.ui.delegate

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.layout_step_quiz_code.view.*
import kotlinx.android.synthetic.main.layout_step_quiz_code_fullscreen_playground.view.codeStepLayout
import kotlinx.android.synthetic.main.layout_step_quiz_code_fullscreen_playground.view.stepQuizActions
import org.stepic.droid.R
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepik.android.model.Reply
import org.stepik.android.model.code.CodeOptions
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.step_quiz.resolver.StepQuizFormResolver
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz_code.mapper.CodeStepQuizFormStateMapper
import org.stepik.android.view.step_quiz_code.model.CodeStepQuizFormState
import org.stepik.android.view.step_quiz_code.ui.adapter.delegate.CodeLangAdapterDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class CodeStepQuizFormDelegate(
    containerView: View,
    private val codeOptions: CodeOptions,
    private val codeLayoutDelegate: CodeLayoutDelegate,
    private val onFullscreenClicked: (lang: String, code: String) -> Unit
) : StepQuizFormDelegate {
    private var state: CodeStepQuizFormState = CodeStepQuizFormState.Idle
        set(value) {
            field = value

            viewStateDelegate.switchState(value)

            when (value) {
                is CodeStepQuizFormState.Lang ->
                    codeLayoutDelegate.setLanguage(value.lang, value.code)
            }
            codeLayoutDelegate.setDetailsContentData((value as? CodeStepQuizFormState.Lang)?.lang)
        }

    private val viewStateDelegate = ViewStateDelegate<CodeStepQuizFormState>()

    private val codeLayout = containerView.codeStepLayout
    private val stepQuizActions = containerView.stepQuizActions

    private val stepQuizCodeLangChooserTitle = containerView.stepQuizCodeLangChooserTitle
    private val stepQuizCodeLangChooser = containerView.stepQuizCodeLangChooser
    private val stepQuizCodeLangChooserAdapter = DefaultDelegateAdapter<String>()

    private val codeStepQuizFormStateMapper = CodeStepQuizFormStateMapper()

    init {
        viewStateDelegate.addState<CodeStepQuizFormState.Idle>()
        viewStateDelegate.addState<CodeStepQuizFormState.NoLang>(stepQuizCodeLangChooserTitle, stepQuizCodeLangChooser,
            containerView.stepQuizCodeLangChooserDividerTop, containerView.stepQuizCodeLangChooserDividerBottom)
        viewStateDelegate.addState<CodeStepQuizFormState.Lang>(codeLayout, stepQuizActions)

        /**
         * Lang chooser
         */
        stepQuizCodeLangChooserAdapter += CodeLangAdapterDelegate { state = CodeStepQuizFormState.Lang(it, codeOptions.codeTemplates[it] ?: "") }
        stepQuizCodeLangChooserAdapter.items = codeOptions.codeTemplates.keys.toList().sorted()

        stepQuizCodeLangChooserTitle.setCompoundDrawables(start = R.drawable.ic_step_quiz_code_lang)
        with(stepQuizCodeLangChooser) {
            layoutManager = LinearLayoutManager(context)
            adapter = stepQuizCodeLangChooserAdapter
        }

        codeLayout.codeEditor.isFocusable = false
        codeLayout.codeEditor.setOnClickListener {
            val oldState = (state as? CodeStepQuizFormState.Lang)
                ?: return@setOnClickListener
            onFullscreenClicked(oldState.lang, oldState.code)
        }
    }

    override fun createReply(): ReplyResult {
        val state = state
        return if (state is CodeStepQuizFormState.Lang) {
            ReplyResult.Success(Reply(code = state.code, language = state.lang))
        } else {
            ReplyResult.Error(codeLayout.context.getString(R.string.step_quiz_code_empty_lang))
        }
    }

    override fun setState(state: StepQuizFeature.State.AttemptLoaded) {
        this.state = codeStepQuizFormStateMapper.mapToFormState(codeOptions, state)

        val isEnabled = StepQuizFormResolver.isQuizEnabled(state)
        codeLayoutDelegate.setEnabled(isEnabled)
    }

    fun onLanguageSelected(lang: String) {
        if (state !is CodeStepQuizFormState.Lang) {
            return
        }
        state = CodeStepQuizFormState.Lang(lang, codeOptions.codeTemplates[lang] ?: "")
    }

    fun updateCodeLayoutFromDialog(lang: String, code: String) {
        state = CodeStepQuizFormState.Lang(lang, code)
    }
}