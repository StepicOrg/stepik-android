package org.stepik.android.view.step_quiz_fullscreen_code.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat.getSystemService
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_step_quiz_code_fullscreen.*
import kotlinx.android.synthetic.main.layout_step_quiz_code_fullscreen_instruction.view.*
import kotlinx.android.synthetic.main.layout_step_quiz_code_fullscreen_playground.*
import kotlinx.android.synthetic.main.layout_step_quiz_code_fullscreen_playground.view.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.fonts.FontType
import org.stepic.droid.fonts.FontsProvider
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.dialogs.ChangeCodeLanguageDialog
import org.stepic.droid.ui.dialogs.ProgrammingLanguageChooserDialogFragment
import org.stepic.droid.ui.dialogs.ResetCodeDialogFragment
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

    interface Callback {
        fun onSyncCodeStateWithParent(lang: String, code: String, onSubmitClicked: Boolean = false)
    }

    private lateinit var callback: Callback

    private var inputMethodManager: InputMethodManager? = null

    @Inject
    internal lateinit var fontsProvider: FontsProvider

    private lateinit var coreCodeStepDelegate: CoreCodeStepDelegate

    private lateinit var instructionsLayout: View
    private lateinit var playgroundLayout: View

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return inflater.inflate(R.layout.activity_step_quiz_code_fullscreen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        callback = targetFragment as Callback

        inputMethodManager = getSystemService(App.getAppContext(), InputMethodManager::class.java)

        fullScreenCodeToolbarTitle.text = lessonData.lesson.title
        fullScreenCodeToolbar.inflateMenu(R.menu.code_playground_menu)
        fullScreenCodeToolbar.setNavigationOnClickListener { dismiss() }
        fullScreenCodeToolbar.setNavigationIcon(R.drawable.ic_close_dark)
        fullScreenCodeToolbar.setOnMenuItemClickListener { item ->
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
            override fun onSubmitClicked() {
                callback.onSyncCodeStateWithParent(lang, codeStepLayout.text.toString(), true)
                dismiss()
            }

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
            keyboardExtensionContainer = coordinator,
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
                    inputMethodManager?.hideSoftInputFromWindow(playgroundLayout.windowToken, 0)
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
}