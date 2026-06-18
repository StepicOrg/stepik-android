package org.stepik.android.view.step_quiz_code.ui.delegate

import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import org.stepic.droid.R
import org.stepic.droid.databinding.LayoutStepQuizCodeBinding
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
    codeStepQuizBinding: LayoutStepQuizCodeBinding,
    private val codeOptions: CodeOptions,
    private val codeLayoutDelegate: CodeLayoutDelegate,
    private val onFullscreenClicked: (lang: String, code: String) -> Unit,
    private val syncCodePreference: (String) -> Unit,
    private val onQuizChanged: (ReplyResult) -> Unit
) : StepQuizFormDelegate {
    constructor(
        containerView: View,
        codeOptions: CodeOptions,
        codeLayoutDelegate: CodeLayoutDelegate,
        onFullscreenClicked: (lang: String, code: String) -> Unit,
        syncCodePreference: (String) -> Unit,
        onQuizChanged: (ReplyResult) -> Unit
    ) : this(
        LayoutStepQuizCodeBinding.bind(containerView.findViewById(R.id.stepQuizCodeContainer)),
        codeOptions,
        codeLayoutDelegate,
        onFullscreenClicked,
        syncCodePreference,
        onQuizChanged
    )

    private val binding = codeStepQuizBinding

    private var state: CodeStepQuizFormState = CodeStepQuizFormState.Idle
        set(value) {
            field = value

            viewStateDelegate.switchState(value)

            when (value) {
                is CodeStepQuizFormState.Lang ->
                    codeLayoutDelegate.setLanguage(value.lang, value.code)
                else -> Unit
            }
            codeLayoutDelegate.setDetailsContentData((value as? CodeStepQuizFormState.Lang)?.lang)
        }

    private val viewStateDelegate = ViewStateDelegate<CodeStepQuizFormState>()

    private val codeLayout = binding.codeStepLayout
    private val stepQuizActions = binding.stepQuizActions

    private val stepQuizCodeLangChooserTitle = binding.stepQuizCodeLangChooserTitle
    private val stepQuizCodeLangChooser = binding.stepQuizCodeLangChooser
    private val stepQuizCodeLangChooserAdapter = DefaultDelegateAdapter<String>()

    private val codeStepQuizFormStateMapper = CodeStepQuizFormStateMapper()

    init {
        viewStateDelegate.addState<CodeStepQuizFormState.Idle>()
        viewStateDelegate.addState<CodeStepQuizFormState.NoLang>(stepQuizCodeLangChooserTitle, stepQuizCodeLangChooser,
            binding.stepQuizCodeLangChooserDividerTop.root, binding.stepQuizCodeLangChooserDividerBottom.root)
        viewStateDelegate.addState<CodeStepQuizFormState.Lang>(codeLayout, stepQuizActions)

        /**
         * Lang chooser
         */
        stepQuizCodeLangChooserAdapter += CodeLangAdapterDelegate {
            val codeTemplate = codeOptions.codeTemplates[it] ?: ""
            syncCodePreference(it)
            state = CodeStepQuizFormState.Lang(it, codeTemplate)
        }
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
        codeLayout.codeEditor.doAfterTextChanged {
            if (state is CodeStepQuizFormState.Idle) return@doAfterTextChanged
            onQuizChanged(createReply())
        }
    }

    override fun createReply(): ReplyResult {
        val state = state
        return if (state is CodeStepQuizFormState.Lang) {
            ReplyResult(Reply(code = state.code, language = state.lang), ReplyResult.Validation.Success)
        } else {
            ReplyResult(Reply(), ReplyResult.Validation.Error(codeLayout.context.getString(R.string.step_quiz_code_empty_lang)))
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
        syncCodePreference(lang)
        state = CodeStepQuizFormState.Lang(lang, codeOptions.codeTemplates[lang] ?: "")
    }

    fun updateCodeLayoutFromDialog(lang: String, code: String) {
        state = CodeStepQuizFormState.Lang(lang, code)
    }
}
