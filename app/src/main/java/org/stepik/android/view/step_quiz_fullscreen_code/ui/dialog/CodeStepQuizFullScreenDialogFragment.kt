package org.stepik.android.view.step_quiz_fullscreen_code.ui.dialog

import android.app.Dialog
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
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
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.fonts.FontType
import org.stepic.droid.fonts.FontsProvider
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.dialogs.ChangeCodeLanguageDialog
import org.stepic.droid.ui.dialogs.ProgrammingLanguageChooserDialogFragment
import org.stepic.droid.ui.dialogs.ResetCodeDialogFragment
import org.stepic.droid.util.argument
import org.stepic.droid.util.setTextColor
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.presentation.step_quiz.StepQuizPresenter
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.step_quiz_code.model.CodeStepQuizFormState
import org.stepik.android.view.step_quiz_code.ui.delegate.CodeQuizInstructionDelegate
import org.stepik.android.view.step_quiz_code.ui.delegate.CodeStepQuizFullScreenFormDelegate
import org.stepik.android.view.step_quiz_fullscreen_code.ui.adapter.CodeStepQuizFullScreenPagerAdapter
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import uk.co.chrisjenx.calligraphy.TypefaceUtils
import javax.inject.Inject

class CodeStepQuizFullScreenDialogFragment : DialogFragment(), StepQuizView, ChangeCodeLanguageDialog.Callback, ProgrammingLanguageChooserDialogFragment.Callback, ResetCodeDialogFragment.Callback {
    companion object {
        const val TAG = "CodeStepQuizFullScreenDialogFragment"
        const val CODE_PLAYGROUND_REQUEST = 153

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

    private lateinit var viewStateDelegate: ViewStateDelegate<StepQuizView.State>

    @Inject
    internal lateinit var fontsProvider: FontsProvider

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var presenter: StepQuizPresenter

    private lateinit var codeStepQuizFormFullScreenDelegate: CodeStepQuizFullScreenFormDelegate

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

        presenter = ViewModelProviders.of(this, viewModelFactory).get(StepQuizPresenter::class.java)

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

        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<StepQuizView.State.Idle>(fullScreenCodeViewPager)
        viewStateDelegate.addState<StepQuizView.State.Loading>(stepQuizProgress)
        viewStateDelegate.addState<StepQuizView.State.AttemptLoaded>(fullScreenCodeViewPager)

        val actionsListener = object : CodeStepQuizFullScreenFormDelegate.ActionsListener {
            override fun onSubmitClicked() {
                callback.onSyncCodeStateWithParent((codeStepQuizFormFullScreenDelegate.state as CodeStepQuizFormState.Lang).lang, codeStepLayout.text.toString(), true)
                dismiss()
            }

            override fun onChangeLanguageClicked() {
                val dialog = ChangeCodeLanguageDialog.newInstance()
                if (!dialog.isAdded) {
                    dialog.show(childFragmentManager, null)
                }
            }
        }
        codeStepQuizFormFullScreenDelegate = CodeStepQuizFullScreenFormDelegate(playgroundLayout, coordinator, stepWrapper, actionsListener, CodeQuizInstructionDelegate(instructionsLayout, false))

        if (savedInstanceState == null) {
            codeStepQuizFormFullScreenDelegate.state = CodeStepQuizFormState.Lang(lang, code)
        } else {
            presenter.onStepData(stepWrapper, lessonData)
        }
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

        presenter.attachView(this)
    }

    override fun onPause() {
        callback.onSyncCodeStateWithParent((codeStepQuizFormFullScreenDelegate.state as CodeStepQuizFormState.Lang).lang, codeStepLayout.text.toString())
        super.onPause()
    }

    override fun onStop() {
        presenter.detachView(this)
        val reply = codeStepQuizFormFullScreenDelegate.createReply()
        if (reply is ReplyResult.Success) {
            presenter.syncReplyState(reply.reply)
        }
        super.onStop()
    }

    override fun setState(state: StepQuizView.State) {
        viewStateDelegate.switchState(state)
        if (state is StepQuizView.State.AttemptLoaded) {
            codeStepQuizFormFullScreenDelegate.setState(state)
        }
    }

    override fun showNetworkError() {
        val view = view
            ?: return

        Snackbar
            .make(view, R.string.connectionProblems, Snackbar.LENGTH_SHORT)
            .setTextColor(ContextCompat.getColor(view.context, R.color.white))
            .show()
    }

    override fun onChangeLanguage() {
        val languages = stepWrapper.step.block?.options?.limits?.keys?.sorted()?.toTypedArray() ?: emptyArray()

        val dialog = ProgrammingLanguageChooserDialogFragment.newInstance(languages)
        if (!dialog.isAdded) {
            dialog.show(childFragmentManager, null)
        }
    }

    override fun onLanguageChosen(programmingLanguage: String) {
        codeStepQuizFormFullScreenDelegate.onLanguageSelected(programmingLanguage)
    }

    override fun onReset() {
        codeStepQuizFormFullScreenDelegate.onResetCode()
    }
}