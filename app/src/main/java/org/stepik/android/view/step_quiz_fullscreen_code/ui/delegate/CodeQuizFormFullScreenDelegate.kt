package org.stepik.android.view.step_quiz_fullscreen_code.ui.delegate

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.fragment_step_quiz_code_fullscreen_playground.view.*
import kotlinx.android.synthetic.main.layout_step_quiz_code.view.codeStepLayout
import org.stepic.droid.R
import org.stepic.droid.code.util.CodeToolbarUtil
import org.stepic.droid.model.code.extensionForLanguage
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.adapters.CodeToolbarAdapter
import org.stepic.droid.ui.util.inflate
import org.stepic.droid.ui.util.setOnKeyboardOpenListener
import org.stepic.droid.util.DpPixelsHelper
import org.stepik.android.model.Reply
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.base.ui.interfaces.KeyboardExtensionContainer
import org.stepik.android.view.step_quiz.resolver.StepQuizFormResolver
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz_code.mapper.CodeStepQuizDetailsMapper
import org.stepik.android.view.step_quiz_code.mapper.CodeStepQuizFormStateMapper
import org.stepik.android.view.step_quiz_code.model.CodeDetail
import org.stepik.android.view.step_quiz_code.model.CodeStepQuizFormState
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapterssupport.DefaultDelegateAdapter

class CodeQuizFormFullScreenDelegate(
    containerView: View,
    keyboardExtensionContainer: KeyboardExtensionContainer?,
    private val stepWrapper: StepPersistentWrapper,
    private val actionsListener: ActionsListener
) : StepQuizFormDelegate {
    private var state: CodeStepQuizFormState = CodeStepQuizFormState.Idle
        set(value) {
            field = value

            viewStateDelegate.switchState(value)

            when (value) {
                is CodeStepQuizFormState.Lang -> {
                    codeLayout.setText(value.code)
                    codeLayout.lang = extensionForLanguage(value.lang)
                    codeToolbarAdapter.setLanguage(value.lang)
                    codeChosenLanguage.setText(value.lang)
                }
            }

            stepQuizCodeDetailsAdapter.items =
                codeStepQuizDetailsMapper.mapToCodeDetails(stepWrapper.step, (value as? CodeStepQuizFormState.Lang)?.lang)
        }

    // Flag is necessary, because keyboard listener is constantly invoked (probably global layout listener reacts to view changes)
    private var keyboardShown: Boolean = false

    private val viewStateDelegate = ViewStateDelegate<CodeStepQuizFormState>()
    private val codeStepQuizFormStateMapper = CodeStepQuizFormStateMapper()

    private val codeLayout = containerView.codeStepLayout
    private val codeChosenLanguage = containerView.codeChosenLanguage
    private val codeSubmitButton = containerView.codeSubmitButton

    private val stepQuizCodeDetailsAdapter = DefaultDelegateAdapter<CodeDetail>()
    private val codeStepQuizDetailsMapper = CodeStepQuizDetailsMapper()

    private val codeOptions = stepWrapper.step.block?.options ?: throw IllegalArgumentException("Code options shouldn't be null")

    private val codeToolbarAdapter = CodeToolbarAdapter(containerView.context)
        .apply {
            onSymbolClickListener = object : CodeToolbarAdapter.OnSymbolClickListener {
                override fun onSymbolClick(symbol: String, offset: Int) {
                    codeLayout.insertText(CodeToolbarUtil.mapToolbarSymbolToPrintable(symbol, codeLayout.indentSize), offset)
                }
            }
        }

    init {
        viewStateDelegate.addState<CodeStepQuizFormState.Idle>()
        viewStateDelegate.addState<CodeStepQuizFormState.Lang>(codeLayout)

        /**
         * Keyboard extension
         */
        (containerView as? ViewGroup)
            keyboardExtensionContainer
            ?.getKeyboardExtensionViewContainer()
            ?.let { container ->
                val stepQuizCodeKeyboardExtension =
                    container.inflate(R.layout.layout_step_quiz_code_keyboard_extension) as RecyclerView
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

        codeChosenLanguage.setOnClickListener { actionsListener.onChangeLanguageClicked() }
        codeSubmitButton.setOnClickListener { actionsListener.onSubmitClicked() }
    }

    override fun setState(state: StepQuizView.State.AttemptLoaded) {
        this.state = codeStepQuizFormStateMapper.mapToFormState(codeOptions, state)

        val isEnabled = StepQuizFormResolver.isQuizEnabled(state)
        codeLayout.isEnabled = isEnabled
        codeSubmitButton.isEnabled = isEnabled
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

    override fun createReply(): ReplyResult {
        val state = state
        return if (state is CodeStepQuizFormState.Lang) {
            ReplyResult.Success(Reply(code = codeLayout.text.toString(), language = state.lang))
        } else {
            ReplyResult.Error(codeLayout.context.getString(R.string.step_quiz_code_empty_lang))
        }
    }

    interface ActionsListener {
        fun onChangeLanguageClicked()
        fun onSubmitClicked()
    }
}