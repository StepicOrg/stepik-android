package org.stepik.android.view.step_quiz_fullscreen_code.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
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
import org.stepik.android.presentation.step_quiz_code.StepQuizCodeRunPresenter
import org.stepik.android.view.step_quiz_code.ui.delegate.CodeLayoutDelegate
import org.stepik.android.view.step_quiz_code.ui.delegate.CodeQuizInstructionDelegate
import org.stepik.android.view.step_quiz_code.ui.delegate.CodeStepRunCodeDelegate
import org.stepik.android.view.step_quiz_fullscreen_code.ui.adapter.CodeStepQuizFullScreenPagerAdapter
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.hideKeyboard
import timber.log.Timber
import javax.inject.Inject

class CodeStepQuizFullScreenDialogFragment : DialogFragment(),
    ChangeCodeLanguageDialog.Callback,
    ProgrammingLanguageChooserDialogFragment.Callback,
    ResetCodeDialogFragment.Callback {
    companion object {
        const val TAG = "CodeStepQuizFullScreenDialogFragment"

        private const val ARG_LANG = "LANG"
        private const val ARG_CODE = "CODE"

        private const val INSTRUCTION_TAB = 0
        private const val CODE_TAB = 1
        private const val RUN_CODE_TAB = 2

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
    private var runCodeDelegate: CodeStepRunCodeDelegate? = null

    private lateinit var instructionsLayout: View
    private lateinit var playgroundLayout: View
    private var runCodeLayout: View? = null

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
    private var runCodeActionSeparator: View? = null
    private var runCodeAction: AppCompatTextView? = null

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

        Timber.d("Code options: ${stepWrapper.step.block?.options}")
        initViewPager(isRunCodeEnabled = stepWrapper.step.block?.options?.isRunUserCodeAllowed ?: false)

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

        runCodeDelegate = runCodeLayout?.let { layout ->
            CodeStepRunCodeDelegate(
                runCodeLayout = layout,
                codeRunPresenter = codeRunPresenter,
                fullScreenCodeTabs = fullScreenCodeTabs,
                codeLayout = codeLayout,
                context = requireContext(),
                stepWrapper = stepWrapper
            )
        }

        runCodeDelegate?.lang = lang

        /**
         *  Run code view binding
         */
        runCodeActionSeparator = runCodeLayout?.runCodeActionSeparator
        runCodeAction = runCodeLayout?.runCodeAction

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
        fullScreenCodeViewPager.setCurrentItem(CODE_TAB, false)

        codeSubmitButton.setOnClickListener {
            (parentFragment as? Callback)
                ?.onSyncCodeStateWithParent(lang, codeLayout.text.toString(), onSubmitClicked = true)
            dismiss()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ARG_LANG, lang)
        outState.putString(ARG_CODE, codeLayout.text.toString())
    }

    private fun initViewPager(isRunCodeEnabled: Boolean) {
        val activity = activity
            ?: return

        val lightFont = ResourcesCompat.getFont(activity, R.font.roboto_light)
        val regularFont = ResourcesCompat.getFont(activity, R.font.roboto_regular)

        val pagerAdapter = CodeStepQuizFullScreenPagerAdapter(activity, isRunCodeEnabled = isRunCodeEnabled)

        fullScreenCodeViewPager.adapter = pagerAdapter
        fullScreenCodeTabs.setupWithViewPager(fullScreenCodeViewPager)
        fullScreenCodeViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {}
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}
            override fun onPageSelected(p0: Int) {
                view?.hideKeyboard()
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

        runCodeLayout = if (isRunCodeEnabled) {
            pagerAdapter.getViewAt(2)
        } else {
            null
        }
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
        runCodeDelegate?.let {
            codeRunPresenter.attachView(it)
        }
    }

    override fun onPause() {
        (parentFragment as? Callback)
            ?.onSyncCodeStateWithParent(lang, codeLayout.text.toString())
        super.onPause()
    }

    override fun onStop() {
        runCodeDelegate?.let {
            it.onDetach()
            codeRunPresenter.detachView(it)
        }
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
        runCodeDelegate?.lang = lang
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
                    if (fullScreenCodeViewPager.currentItem == CODE_TAB && runCodeDelegate != null) {
                        codeRunPresenter.resolveRunCodePopup()
                    }
                    stepQuizCodeKeyboardExtension.visibility = View.GONE
                    codeLayout.isNestedScrollingEnabled = true
                    codeLayout.layoutParams =
                        (codeLayout.layoutParams as RelativeLayout.LayoutParams)
                            .apply {
                                bottomMargin = 0
                            }
                    codeLayout.setPadding(
                        0,
                        0,
                        0,
                        requireContext().resources.getDimensionPixelSize(R.dimen.step_quiz_fullscreen_code_layout_bottom_padding)
                    )
                    setViewsVisibility(needShow = true)
                    keyboardShown = false
                }
            },
            onKeyboardShown = {
                if (!keyboardShown) {
                    // We show the keyboard extension only when "Code" tab is opened
                    if (fullScreenCodeViewPager.currentItem == CODE_TAB) {
                        stepQuizCodeKeyboardExtension.visibility = View.VISIBLE
                    }
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

    /**
     *  Hiding views upon opening keyboard
     */
    private fun setViewsVisibility(needShow: Boolean) {
        submitButtonSeparator.isVisible = needShow
        codeSubmitButton.isVisible = needShow
        centeredToolbar.isVisible = needShow
        fullScreenCodeTabs.isVisible = needShow
        fullScreenCodeSeparator.isVisible = needShow
        runCodeActionSeparator?.isVisible = needShow
        runCodeAction?.isVisible = needShow
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