package org.stepik.android.view.step_quiz_code.ui.delegate

import android.view.View
import kotlinx.android.synthetic.main.layout_step_quiz_code.view.*
import org.stepic.droid.R
import org.stepic.droid.code.util.CodeToolbarUtil
import org.stepic.droid.model.code.extensionForLanguage
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.adapters.CodeToolbarAdapter
import org.stepik.android.model.Reply
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz_code.mapper.CodeStepQuizDetailsMapper
import org.stepik.android.view.step_quiz_code.mapper.CodeStepQuizFormStateMapper
import org.stepik.android.view.step_quiz_code.model.CodeStepQuizFormState
import org.stepik.android.view.ui.delegate.ViewStateDelegate

abstract class CodeQuizFormBaseDelegate(
    codeContainerView: View,
    val stepWrapper: StepPersistentWrapper,
    private val codeQuizInstructionDelegate: CodeQuizInstructionDelegate
) : StepQuizFormDelegate {

    var state: CodeStepQuizFormState = CodeStepQuizFormState.Idle
        set(value) {
            field = value

            viewStateDelegate.switchState(value)

            when (value) {
                is CodeStepQuizFormState.Lang -> {
                    codeLayout.setText(value.code)
                    codeLayout.lang = extensionForLanguage(value.lang)
                    stepQuizActionChangeLang.text = value.lang

                    codeToolbarAdapter.setLanguage(value.lang)
                }
            }

            codeQuizInstructionDelegate.setCodeDetailsData(codeStepQuizDetailsMapper.mapToCodeDetails(stepWrapper.step, (value as? CodeStepQuizFormState.Lang)?.lang))
        }

    protected val viewStateDelegate = ViewStateDelegate<CodeStepQuizFormState>()
    protected val codeStepQuizFormStateMapper = CodeStepQuizFormStateMapper()

    protected val codeLayout = codeContainerView.codeStepLayout
    protected val codeStepQuizDetailsMapper = CodeStepQuizDetailsMapper()

    protected val codeToolbarAdapter = CodeToolbarAdapter(codeContainerView.context)
        .apply {
            onSymbolClickListener = object : CodeToolbarAdapter.OnSymbolClickListener {
                override fun onSymbolClick(symbol: String, offset: Int) {
                    codeLayout.insertText(CodeToolbarUtil.mapToolbarSymbolToPrintable(symbol, codeLayout.indentSize), offset)
                }
            }
        }

    protected val stepQuizActions = codeContainerView.stepQuizActions
    protected val stepQuizActionChangeLang = codeContainerView.stepQuizActionChangeLang

    protected val codeOptions = stepWrapper.step.block?.options ?: throw IllegalArgumentException("Code options shouldn't be null")

    init {
        codeQuizInstructionDelegate.setupCodeDetailView()
    }

    override fun createReply(): ReplyResult {
        val state = state
        return if (state is CodeStepQuizFormState.Lang) {
            ReplyResult.Success(Reply(code = codeLayout.text.toString(), language = state.lang))
        } else {
            ReplyResult.Error(codeLayout.context.getString(R.string.step_quiz_code_empty_lang))
        }
    }

    fun onLanguageSelected(lang: String) {
        if (state !is CodeStepQuizFormState.Lang) {
            return
        }
        state = CodeStepQuizFormState.Lang(lang, codeOptions.codeTemplates[lang] ?: "")
    }

    fun onResetCode() {
        val oldState = (state as? CodeStepQuizFormState.Lang)
            ?: return

        state = oldState.copy(code = codeOptions.codeTemplates[oldState.lang] ?: "")
    }
}