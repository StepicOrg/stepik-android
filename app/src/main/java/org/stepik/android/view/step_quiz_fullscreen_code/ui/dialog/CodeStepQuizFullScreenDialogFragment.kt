package org.stepik.android.view.step_quiz_fullscreen_code.ui.dialog

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.dialog_step_quiz_code_fullscreen.*
import kotlinx.android.synthetic.main.empty_input_samples.view.*
import kotlinx.android.synthetic.main.layout_step_quiz_code_fullscreen_instruction.view.*
import kotlinx.android.synthetic.main.layout_step_quiz_code_fullscreen_playground.view.*
import kotlinx.android.synthetic.main.layout_step_quiz_code_fullscreen_run_code.view.*
import kotlinx.android.synthetic.main.layout_step_quiz_code_keyboard_extension.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import kotlinx.android.synthetic.main.view_step_quiz_submit_button.view.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.code.ui.CodeEditorLayout
import org.stepic.droid.code.util.CodeToolbarUtil
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.adapters.CodeToolbarAdapter
import org.stepic.droid.ui.dialogs.ChangeCodeLanguageDialog
import org.stepic.droid.ui.dialogs.ProgrammingLanguageChooserDialogFragment
import org.stepic.droid.ui.dialogs.ResetCodeDialogFragment
import org.stepic.droid.ui.util.setOnKeyboardOpenListener
import org.stepic.droid.ui.util.snackbar
import org.stepik.android.model.code.UserCodeRun
import org.stepik.android.presentation.step_quiz_code.StepQuizCodeRunPresenter
import org.stepik.android.presentation.step_quiz_code.StepQuizRunCode
import org.stepik.android.view.step_quiz_code.ui.delegate.CodeLayoutDelegate
import org.stepik.android.view.step_quiz_code.ui.delegate.CodeQuizInstructionDelegate
import org.stepik.android.view.step_quiz_fullscreen_code.ui.adapter.CodeStepQuizFullScreenPagerAdapter
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.hideKeyboard
import javax.inject.Inject

class CodeStepQuizFullScreenDialogFragment : DialogFragment(),
    ChangeCodeLanguageDialog.Callback,
    ProgrammingLanguageChooserDialogFragment.Callback,
    ResetCodeDialogFragment.Callback,
    StepQuizRunCode {
    companion object {
        const val TAG = "CodeStepQuizFullScreenDialogFragment"

        private const val ARG_LANG = "LANG"
        private const val ARG_CODE = "CODE"

        fun newInstance(lang: String, code: String, codeTemplates: Map<String, String>, stepPersistentWrapper: StepPersistentWrapper, lessonTitle: String): DialogFragment =
            CodeStepQuizFullScreenDialogFragment()
                .apply {
                    this.lang = lang
                    this.code = code
                    this.codeTemplates = codeTemplates
                    this.stepWrapper = stepPersistentWrapper
                    this.lessonTitle = lessonTitle
                }
    }

    private lateinit var codeLayoutDelegate: CodeLayoutDelegate

    private lateinit var instructionsLayout: View
    private lateinit var playgroundLayout: View
    private lateinit var runCodeLayout: View

    /**
     *  Code play ground views
     */
    private lateinit var codeLayout: CodeEditorLayout
    private lateinit var submitButtonSeparator: View
    private lateinit var codeSubmitButton: View
    private lateinit var retryButton: View

    private lateinit var codeToolbarAdapter: CodeToolbarAdapter

    // Flag is necessary, because keyboard listener is constantly invoked (probably global layout listener reacts to view changes)
    private var keyboardShown: Boolean = false

    /**
     * Run code views
     */
    private lateinit var runCodeEmptyInput: View
    private lateinit var runCodeScrollView: ScrollView
    private lateinit var runCodeInputSamplePicker: AppCompatTextView
    private lateinit var runCodeInputDataSample: AppCompatTextView
    private lateinit var runCodeOutputDataTitle: AppCompatTextView
    private lateinit var runCodeOutputDataSample: AppCompatTextView
    private lateinit var runCodeActionSeparator: View
    private lateinit var runCodeFeedback: AppCompatTextView
    private lateinit var runCodeAction: AppCompatTextView

    private var lang: String by argument()
    private var code: String by argument()
    private var codeTemplates: Map<String, String> by argument()
    private var lessonTitle: String by argument()
    private var stepWrapper: StepPersistentWrapper by argument()

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var codeRunPresenter: StepQuizCodeRunPresenter

    private fun injectComponent() {
        App.component()
            .userCodeRunComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.AppTheme_FullScreenDialog)

        injectComponent()
        codeRunPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(StepQuizCodeRunPresenter::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.dialog_step_quiz_code_fullscreen, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        centeredToolbarTitle.text = lessonTitle
        centeredToolbar.inflateMenu(R.menu.code_playground_menu)
        centeredToolbar.setNavigationOnClickListener { dismiss() }
        centeredToolbar.setNavigationIcon(R.drawable.ic_close_dark)
        centeredToolbar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.new_primary_color))
        centeredToolbar.setOnMenuItemClickListener { item ->
            if (item?.itemId == R.id.action_reset_code) {
                val dialog = ResetCodeDialogFragment.newInstance()
                if (!dialog.isAdded) {
                    dialog.show(childFragmentManager, null)
                }
                true
            } else {
                false
            }
        }

        initViewPager()

        val text = stepWrapper
            .step
            .block
            ?.text
            ?.takeIf(String::isNotEmpty)

        instructionsLayout.stepQuizCodeTextContent.setText(text)

        if (savedInstanceState != null) {
            lang = savedInstanceState.getString(ARG_LANG) ?: return
            code = savedInstanceState.getString(ARG_CODE) ?: return
        }

        /**
         *  Code play ground view binding
         */
        submitButtonSeparator = playgroundLayout.submitButtonSeparator
        codeSubmitButton = playgroundLayout.codeSubmitButton
        retryButton = playgroundLayout.stepQuizRetry
        codeLayout = playgroundLayout.codeStepLayout

        /**
         *  Run code view binding
         */
        runCodeEmptyInput = runCodeLayout.empty_input_samples
        runCodeScrollView = runCodeLayout.dataScrollView
        runCodeInputSamplePicker = runCodeLayout.inputDataSamplePicker
        runCodeInputDataSample = runCodeLayout.inputDataSample
        runCodeOutputDataTitle = runCodeLayout.outputDataTitle
        runCodeOutputDataSample = runCodeLayout.outputDataSample
        runCodeActionSeparator = runCodeLayout.runCodeActionSeparator
        runCodeFeedback = runCodeLayout.runCodeFeedback
        runCodeAction = runCodeLayout.runCodeAction

        retryButton.isVisible = false
        setupCodeToolAdapter()
        setupKeyboardExtension()

        codeLayoutDelegate = CodeLayoutDelegate(
            codeContainerView = playgroundLayout,
            step = stepWrapper.step,
            codeTemplates = codeTemplates,
            codeQuizInstructionDelegate = CodeQuizInstructionDelegate(instructionsLayout, false),
            codeToolbarAdapter = codeToolbarAdapter,
            onChangeLanguageClicked = ::onChangeLanguageClicked
        )

        codeLayoutDelegate.setLanguage(lang, code)
        codeLayoutDelegate.setDetailsContentData(lang)
        fullScreenCodeViewPager.setCurrentItem(1, false)

        codeSubmitButton.setOnClickListener {
            (parentFragment as? Callback)
                ?.onSyncCodeStateWithParent(lang, codeLayout.text.toString(), onSubmitClicked = true)
            dismiss()
        }

        val inputSamples = stepWrapper
            .step
            .block
            ?.options
            ?.samples
            ?.mapIndexed { index, samples -> getString(R.string.step_quiz_code_spinner_item, index + 1, samples.first()) }
            ?: emptyList()

        val popupWindow = ListPopupWindow(requireContext())

        popupWindow.setAdapter(
            ArrayAdapter<String>(
                requireContext(),
                R.layout.run_code_spinner_item,
                inputSamples
            )
        )

        popupWindow.setOnItemClickListener { _, _, position, _ ->
            runCodeInputDataSample.text = inputSamples[position]
                .split(":")
                .last()
                .trim()
            popupWindow.dismiss()
        }

        popupWindow.anchorView = runCodeInputSamplePicker
        popupWindow.width = resources.getDimensionPixelSize(R.dimen.step_quiz_full_screen_code_layout_drop_down_width)
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT

        runCodeInputSamplePicker.setOnClickListener { popupWindow.show() }
        runCodeInputSamplePicker.supportCompoundDrawablesTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.violet1))

        runCodeAction.setOnClickListener {
            codeRunPresenter.createUserCodeRun(
                code = codeLayout.text.toString(),
                language = lang,
                stdin = runCodeInputDataSample.text.toString(),
                stepId = stepWrapper.step.id
            )
        }
        codeRunPresenter.setDataToPresenter(hasSamples = inputSamples.isNotEmpty())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ARG_LANG, lang)
        outState.putString(ARG_CODE, codeLayout.text.toString())
    }

    private fun initViewPager() {
        val activity = activity
            ?: return

        val lightFont = ResourcesCompat.getFont(activity, R.font.roboto_light)
        val regularFont = ResourcesCompat.getFont(activity, R.font.roboto_regular)

        val pagerAdapter = CodeStepQuizFullScreenPagerAdapter(activity)

        fullScreenCodeViewPager.adapter = pagerAdapter
        fullScreenCodeTabs.setupWithViewPager(fullScreenCodeViewPager)
        fullScreenCodeViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {}
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}
            override fun onPageSelected(p0: Int) {
                if (p0 == 0) {
                    playgroundLayout.hideKeyboard()
                }
            }
        })
        fullScreenCodeTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {
                (tab?.customView as? TextView)?.let {
                    it.typeface = lightFont
                }
            }
            override fun onTabSelected(tab: TabLayout.Tab?) {
                (tab?.customView as? TextView)?.let {
                    it.typeface = regularFont
                }
            }
        })

        for (i in 0 until fullScreenCodeTabs.tabCount) {
            val tab = fullScreenCodeTabs.getTabAt(i)
            tab?.customView = layoutInflater.inflate(R.layout.view_course_tab, null)
        }

        (fullScreenCodeTabs.getTabAt(fullScreenCodeTabs.selectedTabPosition)?.customView as? TextView)
            ?.typeface = regularFont

        instructionsLayout = pagerAdapter.getViewAt(0)
        playgroundLayout = pagerAdapter.getViewAt(1)
        runCodeLayout = pagerAdapter.getViewAt(2)
    }

    override fun onStart() {
        super.onStart()
        dialog
            ?.window
            ?.let { window ->
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,  ViewGroup.LayoutParams.MATCH_PARENT)
                window.setWindowAnimations(R.style.AppTheme_FullScreenDialog)
            }
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        codeRunPresenter.attachView(this)
    }

    override fun onPause() {
        (parentFragment as? Callback)
            ?.onSyncCodeStateWithParent(lang, codeLayout.text.toString())
        super.onPause()
    }

    override fun onStop() {
        codeRunPresenter.detachView(this)
        super.onStop()
    }

    override fun onChangeLanguage() {
        val languages = codeTemplates.keys.sorted().toTypedArray()

        val dialog = ProgrammingLanguageChooserDialogFragment.newInstance(languages)
        if (!dialog.isAdded) {
            dialog.show(childFragmentManager, null)
        }
    }

    override fun onLanguageChosen(programmingLanguage: String) {
        lang = programmingLanguage
        codeLayoutDelegate.setLanguage(programmingLanguage)
        codeLayoutDelegate.setDetailsContentData(programmingLanguage)
    }

    override fun onReset() {
        codeLayoutDelegate.setLanguage(lang)
    }

    private fun setupCodeToolAdapter() {
        codeToolbarAdapter = CodeToolbarAdapter(requireContext())
            .apply {
                onSymbolClickListener = object : CodeToolbarAdapter.OnSymbolClickListener {
                    override fun onSymbolClick(symbol: String, offset: Int) {
                        codeLayout.insertText(CodeToolbarUtil.mapToolbarSymbolToPrintable(symbol, codeLayout.indentSize), offset)
                    }
                }
            }
    }

    /**
     * Keyboard extension
     */
    private fun setupKeyboardExtension() {
        stepQuizCodeKeyboardExtension.adapter = codeToolbarAdapter
        stepQuizCodeKeyboardExtension.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        codeLayout.codeToolbarAdapter = codeToolbarAdapter

        setOnKeyboardOpenListener(
            coordinator,
            onKeyboardHidden = {
                if (keyboardShown) {
                    stepQuizCodeKeyboardExtension.visibility = View.GONE
                    codeLayout.isNestedScrollingEnabled = true
                    codeLayout.layoutParams =
                        (codeLayout.layoutParams as RelativeLayout.LayoutParams)
                            .apply {
                                bottomMargin = 0
                            }
                    codeLayout.setPadding(0, 0, 0, requireContext().resources.getDimensionPixelSize(
                        R.dimen.step_quiz_fullscreen_code_layout_bottom_padding))
                    setViewsVisibility(needShow = true)
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
                    setViewsVisibility(needShow = false)
                    keyboardShown = true
                }
            }
        )
    }

    override fun setState(state: StepQuizRunCode.State) {
        setStateVisibility(state)
        val isEnabled = state is StepQuizRunCode.State.Idle ||
                (state is StepQuizRunCode.State.UserCodeRunLoaded && state.userCodeRun.status != UserCodeRun.Status.EVALUATION)

        runCodeAction.isEnabled = isEnabled
        runCodeInputSamplePicker.isEnabled = isEnabled

        if (state is StepQuizRunCode.State.UserCodeRunLoaded) {
            when (state.userCodeRun.status) {
                UserCodeRun.Status.SUCCESS ->
                    setOutputText(state.userCodeRun.stdout)
                UserCodeRun.Status.FAILURE ->
                    setOutputText(state.userCodeRun.stderr)
                else ->
                    Unit
            }
        }
    }

    private fun setStateVisibility(state: StepQuizRunCode.State) {
        if (state is StepQuizRunCode.State.Empty) {
            runCodeEmptyInput.isVisible = true
            runCodeScrollView.isVisible = false
            runCodeActionSeparator.isVisible = false
            runCodeAction.isVisible = false
        }

        runCodeFeedback.isVisible = state is StepQuizRunCode.State.Loading

        if (state is StepQuizRunCode.State.UserCodeRunLoaded) {
            when (state.userCodeRun.status) {
                UserCodeRun.Status.SUCCESS -> {
                    runCodeFeedback.isVisible = false
                    runCodeOutputDataTitle.isVisible = true
                    runCodeOutputDataSample.isVisible = true
                }
                UserCodeRun.Status.FAILURE -> {
                    runCodeFeedback.isVisible = false
                    runCodeOutputDataTitle.isVisible = true
                    runCodeOutputDataSample.isVisible = true
                }
                UserCodeRun.Status.EVALUATION -> {
                    runCodeFeedback.isVisible = true
                }
            }
        }
    }

    private fun setOutputText(text: String?) {
        if (text.isNullOrEmpty()) {
            runCodeOutputDataSample.text = getString(R.string.step_quiz_code_empty_output)
        } else {
            runCodeOutputDataSample.text = text
        }
    }

    override fun showNetworkError() {
        view?.snackbar(messageRes = R.string.connectionProblems)
    }

    /**
     *  Hiding views upon opening keyboard
     */
    private fun setViewsVisibility(needShow: Boolean) {
        submitButtonSeparator.isVisible = needShow
        codeSubmitButton.isVisible = needShow
        centeredToolbar.isVisible = needShow
        fullScreenCodeTabs.isVisible = needShow
        fullScreenCodeSeparator.isVisible = needShow
    }

    private fun onChangeLanguageClicked() {
        val dialog = ChangeCodeLanguageDialog.newInstance()
        if (!dialog.isAdded) {
            dialog.show(childFragmentManager, null)
        }
    }

    interface Callback {
        fun onSyncCodeStateWithParent(lang: String, code: String, onSubmitClicked: Boolean = false)
    }
}