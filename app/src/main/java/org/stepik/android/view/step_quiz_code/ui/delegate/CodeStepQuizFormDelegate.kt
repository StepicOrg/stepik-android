package org.stepik.android.view.step_quiz_code.ui.delegate

import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.PopupMenu
import kotlinx.android.synthetic.main.layout_step_quiz_code.view.*
import kotlinx.android.synthetic.main.layout_step_quiz_code.view.stepQuizCodeDetails
import kotlinx.android.synthetic.main.layout_step_quiz_code.view.stepQuizCodeDetailsArrow
import kotlinx.android.synthetic.main.layout_step_quiz_code.view.stepQuizCodeDetailsContent
import org.stepic.droid.R
import org.stepic.droid.code.util.CodeToolbarUtil
import org.stepic.droid.model.code.extensionForLanguage
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.adapters.CodeToolbarAdapter
import org.stepic.droid.ui.util.StepikAnimUtils
import org.stepic.droid.ui.util.inflate
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepic.droid.ui.util.setOnKeyboardOpenListener
import org.stepic.droid.util.ColorUtil
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
import org.stepik.android.view.step_quiz_code.ui.adapter.delegate.CodeDetailLimitAdapterDelegate
import org.stepik.android.view.step_quiz_code.ui.adapter.delegate.CodeDetailSampleAdapterDelegate
import org.stepik.android.view.step_quiz_code.ui.adapter.delegate.CodeLangAdapterDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.ui.adapterssupport.DefaultDelegateAdapter

class CodeStepQuizFormDelegate(
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
                    stepQuizActionChangeLang.text = value.lang

                    codeToolbarAdapter.setLanguage(value.lang)
                }
            }

            stepQuizCodeDetailsAdapter.items =
                codeStepQuizDetailsMapper.mapToCodeDetails(stepWrapper.step, (value as? CodeStepQuizFormState.Lang)?.lang)
        }

    private val viewStateDelegate = ViewStateDelegate<CodeStepQuizFormState>()
    private val codeStepQuizFormStateMapper = CodeStepQuizFormStateMapper()


    private val codeLayout = containerView.codeStepLayout

    private val stepQuizCodeDetails = containerView.stepQuizCodeDetails
    private val stepQuizCodeDetailsArrow = containerView.stepQuizCodeDetailsArrow
    private val stepQuizCodeDetailsContent = containerView.stepQuizCodeDetailsContent

    private val stepQuizCodeDetailsAdapter = DefaultDelegateAdapter<CodeDetail>()
    private val codeStepQuizDetailsMapper = CodeStepQuizDetailsMapper()


    private val codeToolbarAdapter = CodeToolbarAdapter(containerView.context)
        .apply {
            onSymbolClickListener = object : CodeToolbarAdapter.OnSymbolClickListener {
                override fun onSymbolClick(symbol: String, offset: Int) {
                    codeLayout.insertText(CodeToolbarUtil.mapToolbarSymbolToPrintable(symbol, codeLayout.indentSize), offset)
                }
            }
        }


    private val stepQuizCodeLangChooserTitle = containerView.stepQuizCodeLangChooserTitle
    private val stepQuizCodeLangChooser = containerView.stepQuizCodeLangChooser
    private val stepQuizCodeLangChooserAdapter = DefaultDelegateAdapter<String>()


    private val stepQuizActions = containerView.stepQuizActions
    private val stepQuizActionChangeLang = containerView.stepQuizActionChangeLang
    private val stepQuizActionFullscreen = containerView.stepQuizActionFullscreen
    private val stepQuizActionMore = containerView.stepQuizActionMore


    private val codeOptions = stepWrapper.step.block?.options ?: throw IllegalArgumentException("Code options shouldn't be null")

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

        /**
         * Keyboard extension
         */
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

        stepQuizActionFullscreen.setOnClickListener {
            val state = (state as? CodeStepQuizFormState.Lang) ?: return@setOnClickListener
            actionsListener.onFullscreenClicked(state.lang, codeLayout.text.toString())
        }
        stepQuizActionMore.setOnClickListener { showMoreActions() }
    }

    private fun showMoreActions() {
        val context = stepQuizActionMore.context
        val popupMenu = PopupMenu(context, stepQuizActionMore)
        popupMenu.inflate(R.menu.code_playground_menu)

        val menu = popupMenu.menu
        val resetMenuItem = menu?.findItem(R.id.action_reset_code)
        val resetString = SpannableString(context.getString(R.string.code_quiz_reset))
        resetString.setSpan(ForegroundColorSpan(ColorUtil.getColorArgb(R.color.new_red_color)), 0, resetString.length, 0)
        resetMenuItem?.title = resetString

        menu?.findItem(R.id.action_language_code)?.isVisible = false

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_reset_code ->
                    actionsListener.onResetClicked()
            }
            true
        }
        popupMenu.show()
    }

    override fun createReply(): ReplyResult =
        ReplyResult.Success(Reply(code = codeLayout.text.toString(), language = codeLayout.lang))

    override fun setState(state: StepQuizView.State.AttemptLoaded) {
        this.state = codeStepQuizFormStateMapper.mapToFormState(codeOptions, state)

        val isEnabled = StepQuizFormResolver.isQuizEnabled(state)
        codeLayout.isEnabled = isEnabled
        stepQuizActionChangeLang.isEnabled = isEnabled
        stepQuizActionFullscreen.isEnabled = isEnabled
        stepQuizActionMore.isEnabled = isEnabled
    }

    fun onLanguageSelected(lang: String) {
        if (state !is CodeStepQuizFormState.Lang) {
            return
        }

        state = CodeStepQuizFormState.Lang(lang, codeOptions.codeTemplates[lang] ?: "")
    }

    fun onResultFromFullscreen(lang: String, code: String) {
        state = CodeStepQuizFormState.Lang(lang, code)
    }

    fun onResetCode() {
        val oldState = (state as? CodeStepQuizFormState.Lang)
            ?: return

        state = oldState.copy(code = codeOptions.codeTemplates[oldState.lang] ?: "")
    }

    interface ActionsListener {
        fun onChangeLanguageClicked()
        fun onFullscreenClicked(lang: String, code: String)
        fun onResetClicked()
    }
}