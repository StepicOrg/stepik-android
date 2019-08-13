package org.stepik.android.view.step_quiz_code.ui.delegate

import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.layout_step_quiz_code.view.*
import org.stepic.droid.R
import org.stepic.droid.code.util.CodeToolbarUtil
import org.stepic.droid.model.code.extensionForLanguage
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.adapters.CodeToolbarAdapter
import org.stepik.android.model.Reply
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.step_quiz.resolver.StepQuizFormResolver
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz_code.mapper.CodeStepQuizDetailsMapper
import org.stepik.android.view.step_quiz_code.mapper.CodeStepQuizFormStateMapper
import org.stepik.android.view.step_quiz_code.model.CodeDetail
import org.stepik.android.view.step_quiz_code.model.CodeStepQuizFormState
import org.stepik.android.view.step_quiz_code.ui.adapter.delegate.CodeDetailLimitAdapterDelegate
import org.stepik.android.view.step_quiz_code.ui.adapter.delegate.CodeDetailSampleAdapterDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapterssupport.DefaultDelegateAdapter

abstract class CodeQuizFormBaseDelegate(
    detailsContainerView: View,
    codeContainerView: View,
    val stepWrapper: StepPersistentWrapper
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

            stepQuizCodeDetailsAdapter.items =
                codeStepQuizDetailsMapper.mapToCodeDetails(stepWrapper.step, (value as? CodeStepQuizFormState.Lang)?.lang)
        }

    protected val viewStateDelegate = ViewStateDelegate<CodeStepQuizFormState>()
    protected val codeStepQuizFormStateMapper = CodeStepQuizFormStateMapper()

    protected val codeLayout = codeContainerView.codeStepLayout

    protected val stepQuizCodeDetails = detailsContainerView.stepQuizCodeDetails
    protected val stepQuizCodeDetailsArrow = detailsContainerView.stepQuizCodeDetailsArrow
    protected val stepQuizCodeDetailsContent = detailsContainerView.stepQuizCodeDetailsContent

    protected val stepQuizCodeDetailsAdapter = DefaultDelegateAdapter<CodeDetail>()
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

    protected fun setupCodeDetailContentData() {
        stepQuizCodeDetailsAdapter += CodeDetailSampleAdapterDelegate()
        stepQuizCodeDetailsAdapter += CodeDetailLimitAdapterDelegate()

        with(stepQuizCodeDetailsContent) {
            visibility = View.GONE
            layoutManager = LinearLayoutManager(context)
            adapter = stepQuizCodeDetailsAdapter

            val divider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            divider.setDrawable(AppCompatResources.getDrawable(context, R.drawable.bg_step_quiz_code_details_separator)!!)
            addItemDecoration(divider)
        }
    }

    override fun createReply(): ReplyResult {
        val state = state
        return if (state is CodeStepQuizFormState.Lang) {
            ReplyResult.Success(Reply(code = codeLayout.text.toString(), language = state.lang))
        } else {
            ReplyResult.Error(codeLayout.context.getString(R.string.step_quiz_code_empty_lang))
        }
    }

    override fun setState(state: StepQuizView.State.AttemptLoaded) {
        this.state = codeStepQuizFormStateMapper.mapToFormState(codeOptions, state)

        val isEnabled = StepQuizFormResolver.isQuizEnabled(state)
        codeLayout.isEnabled = isEnabled
        stepQuizActionChangeLang.isEnabled = isEnabled
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