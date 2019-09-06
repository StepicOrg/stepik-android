package org.stepik.android.view.step_quiz_fullscreen_code.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_step_quiz_code_fullscreen.*
import kotlinx.android.synthetic.main.layout_step_quiz_code_fullscreen_instruction.view.*
import kotlinx.android.synthetic.main.layout_step_quiz_code_fullscreen_playground.*
import kotlinx.android.synthetic.main.layout_step_quiz_code_fullscreen_playground.view.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import kotlinx.android.synthetic.main.view_step_quiz_submit_button.view.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.code.ui.CodeEditorLayout
import org.stepic.droid.fonts.FontType
import org.stepic.droid.fonts.FontsProvider
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.dialogs.ChangeCodeLanguageDialog
import org.stepic.droid.ui.dialogs.ProgrammingLanguageChooserDialogFragment
import org.stepic.droid.ui.dialogs.ResetCodeDialogFragment
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.ui.util.hideKeyboard
import org.stepic.droid.ui.util.inflate
import org.stepic.droid.ui.util.setOnKeyboardOpenListener
import org.stepic.droid.util.argument
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.view.step_quiz_code.model.CodeStepQuizFormState
import org.stepik.android.view.step_quiz_code.ui.delegate.CodeQuizInstructionDelegate
import org.stepik.android.view.step_quiz_code.ui.delegate.CoreCodeStepDelegate
import org.stepik.android.view.step_quiz_fullscreen_code.ui.adapter.CodeStepQuizFullScreenPagerAdapter
import uk.co.chrisjenx.calligraphy.TypefaceUtils
import javax.inject.Inject

class CodeStepQuizFullScreenDialogFragment : DialogFragment(), ChangeCodeLanguageDialog.Callback, ProgrammingLanguageChooserDialogFragment.Callback, ResetCodeDialogFragment.Callback {
    companion object {
        const val TAG = "CodeStepQuizFullScreenDialogFragment"
        const val CODE_PLAYGROUND_REQUEST = 153

        private const val LANG = "LANG"
        private const val CODE = "CODE"

        fun newInstance(lang: String, code: String, stepPersistentWrapper: StepPersistentWrapper, lessonData: LessonData): DialogFragment =
            CodeStepQuizFullScreenDialogFragment()
                .apply {
                    this.lang = lang
                    this.code = code
                    this.stepWrapper = stepPersistentWrapper
                    this.lessonData = lessonData
                }
    }

    @Inject
    internal lateinit var fontsProvider: FontsProvider

    private lateinit var callback: Callback

    private lateinit var coreCodeStepDelegate: CoreCodeStepDelegate

    private lateinit var instructionsLayout: View
    private lateinit var playgroundLayout: View

    private lateinit var codeLayout: CodeEditorLayout
    private lateinit var submitButtonSeparator: View
    private lateinit var codeSubmitButton: View
    private lateinit var retryButton: View

    // Flag is necessary, because keyboard listener is constantly invoked (probably global layout listener reacts to view changes)
    private var keyboardShown: Boolean = false

    private var lang: String by argument()
    private var code: String by argument()
    private var lessonData: LessonData by argument()
    private var stepWrapper: StepPersistentWrapper by argument()

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
    }

    private fun injectComponent() {
        App.component()
            .stepComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.activity_step_quiz_code_fullscreen, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        callback = targetFragment as Callback

        centeredToolbarTitle.text = lessonData.lesson.title
        centeredToolbar.inflateMenu(R.menu.code_playground_menu)
        centeredToolbar.setNavigationOnClickListener { dismiss() }
        centeredToolbar.setNavigationIcon(R.drawable.ic_close_dark)
        centeredToolbar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.new_primary_color))
        centeredToolbar.setOnMenuItemClickListener { item ->
            when (item?.itemId) {
                android.R.id.home -> {
                    true
                }
                R.id.action_reset_code -> {
                    val dialog = ResetCodeDialogFragment.newInstance()
                    if (!dialog.isAdded) {
                        dialog.show(childFragmentManager, null)
                    }
                    true
                }
                else -> false
            }
        }

        initViewPager()

        val text = stepWrapper
            .step
            .block
            ?.text
            ?.takeIf(String::isNotEmpty)

        if (text != null) {
            instructionsLayout.stepQuizCodeTextContent.setText(text)
            instructionsLayout.stepQuizCodeTextContent.setTextSize(16f)
            instructionsLayout.stepQuizCodeTextContent.setTextIsSelectable(true)
        }

        val actionsListener = object : CoreCodeStepDelegate.ActionsListener {
            override fun onChangeLanguageClicked() {
                val dialog = ChangeCodeLanguageDialog.newInstance()
                if (!dialog.isAdded) {
                    dialog.show(childFragmentManager, null)
                }
            }

            override fun onFullscreenClicked(lang: String, code: String) {}
        }

        coreCodeStepDelegate = CoreCodeStepDelegate(
            codeContainerView = playgroundLayout,
            stepWrapper = stepWrapper,
            codeQuizInstructionDelegate = CodeQuizInstructionDelegate(instructionsLayout, false),
            actionsListener = actionsListener
        )

        if (savedInstanceState != null) {
            lang = savedInstanceState.getString(LANG) ?: return
            code = savedInstanceState.getString(CODE) ?: return
        }

        coreCodeStepDelegate.setLanguage(CodeStepQuizFormState.Lang(lang, code))
        coreCodeStepDelegate.setDetailsContentData(lang)
        fullScreenCodeViewPager.setCurrentItem(1, false)

        submitButtonSeparator = playgroundLayout.submitButtonSeparator
        codeSubmitButton = playgroundLayout.codeSubmitButton
        retryButton = playgroundLayout.stepQuizRetry
        codeLayout = playgroundLayout.codeStepLayout

        codeSubmitButton.setOnClickListener {
            callback.onSyncCodeStateWithParent(lang, codeStepLayout.text.toString(), true)
            dismiss()
        }
        retryButton.changeVisibility(false)
        setupKeyboardExtensions()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(LANG, lang)
        outState.putString(CODE, codeStepLayout.text.toString())
    }

    private fun initViewPager() {
        val activity = activity
            ?: return

        val lightFont = TypefaceUtils.load(activity.assets, fontsProvider.provideFontPath(FontType.light))
        val regularFont = TypefaceUtils.load(activity.assets, fontsProvider.provideFontPath(FontType.regular))

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
    }

    override fun onPause() {
        callback.onSyncCodeStateWithParent(lang, codeStepLayout.text.toString())
        super.onPause()
    }

    override fun onChangeLanguage() {
        val languages = stepWrapper.step.block?.options?.limits?.keys?.sorted()?.toTypedArray() ?: emptyArray()

        val dialog = ProgrammingLanguageChooserDialogFragment.newInstance(languages)
        if (!dialog.isAdded) {
            dialog.show(childFragmentManager, null)
        }
    }

    override fun onLanguageChosen(programmingLanguage: String) {
        lang = programmingLanguage
        coreCodeStepDelegate.setLanguage(coreCodeStepDelegate.onLanguageSelected(programmingLanguage))
        coreCodeStepDelegate.setDetailsContentData(programmingLanguage)
    }

    override fun onReset() {
        coreCodeStepDelegate.onResetCode().let { codeTemplate ->
            code = codeTemplate
            playgroundLayout.codeStepLayout.setText(codeTemplate)
        }
    }

    private fun setupKeyboardExtensions() {
        /**
         * Keyboard extension
         */
        coordinator.let { container ->
            val stepQuizCodeKeyboardExtension =
                container.inflate(R.layout.layout_step_quiz_code_keyboard_extension) as RecyclerView
            stepQuizCodeKeyboardExtension.adapter = coreCodeStepDelegate.codeToolbarAdapter
            stepQuizCodeKeyboardExtension.layoutManager = LinearLayoutManager(container.context, LinearLayoutManager.HORIZONTAL, false)
            codeLayout.codeToolbarAdapter = coreCodeStepDelegate.codeToolbarAdapter

            container.addView(stepQuizCodeKeyboardExtension)
            stepQuizCodeKeyboardExtension.visibility = View.INVISIBLE // Apparently this fixes the offset bug when the current line is under the code toolbar adapter
            stepQuizCodeKeyboardExtension.layoutParams = (stepQuizCodeKeyboardExtension.layoutParams as RelativeLayout.LayoutParams)
                .apply {
                    addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                }

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
                        codeLayout.setPadding(0, 0, 0, container.context.resources.getDimensionPixelSize(
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
    }

    /**
     *  Hiding views upon opening keyboard
     */
    private fun setViewsVisibility(needShow: Boolean) {
        submitButtonSeparator.changeVisibility(needShow)
        codeSubmitButton.changeVisibility(needShow)
        centeredToolbar.changeVisibility(needShow)
        fullScreenCodeTabs.changeVisibility(needShow)
        fullScreenCodeSeparator.changeVisibility(needShow)
    }

    interface Callback {
        fun onSyncCodeStateWithParent(lang: String, code: String, onSubmitClicked: Boolean = false)
    }
}