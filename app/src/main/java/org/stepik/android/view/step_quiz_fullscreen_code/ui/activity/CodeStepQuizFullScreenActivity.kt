package org.stepik.android.view.step_quiz_fullscreen_code.ui.activity

import android.app.Activity
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_profile_edit_info.*
import kotlinx.android.synthetic.main.activity_step_quiz_code_fullscreen.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.fonts.FontType
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.dialogs.ChangeCodeLanguageDialog
import org.stepic.droid.ui.dialogs.ProgrammingLanguageChooserDialogFragment
import org.stepic.droid.ui.dialogs.ResetCodeDialogFragment
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.util.setTextColor
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.presentation.step_quiz.StepQuizPresenter
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.step_quiz_code.ui.delegate.CodeStepQuizFullScreenFormDelegate
import org.stepik.android.view.step_quiz_fullscreen_code.ui.adapter.CodeStepQuizFullScreenPagerAdapter
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import uk.co.chrisjenx.calligraphy.TypefaceUtils
import javax.inject.Inject

class CodeStepQuizFullScreenActivity : FragmentActivityBase(), StepQuizView, ChangeCodeLanguageDialog.Callback, ProgrammingLanguageChooserDialogFragment.Callback, ResetCodeDialogFragment.Callback {
    companion object {
        const val IS_SUBMITTED_CLICKED = "is_submit_clicked"
        private const val EXTRA_STEP_WRAPPER = "step_wrapper"
        private const val EXTRA_LESSON_DATA = "lesson_data"

        fun createIntent(context: Context, stepPersistentWrapper: StepPersistentWrapper, lessonData: LessonData): Intent =
            Intent(context, CodeStepQuizFullScreenActivity::class.java)
                .putExtra(EXTRA_STEP_WRAPPER, stepPersistentWrapper)
                .putExtra(EXTRA_LESSON_DATA, lessonData)
    }

    private val inputMethodService = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    private lateinit var stepPersistentWrapper: StepPersistentWrapper
    private lateinit var lessonData: LessonData

    private lateinit var viewStateDelegate: ViewStateDelegate<StepQuizView.State>

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var presenter: StepQuizPresenter

    private lateinit var codeStepQuizFormFullScreenDelegate: CodeStepQuizFullScreenFormDelegate

    private lateinit var instructionsLayout: View
    private lateinit var playgroundLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step_quiz_code_fullscreen)

        injectComponent()
        setSupportActionBar(fullScreenCodeToolbar)
        val actionBar = this.supportActionBar
            ?: throw IllegalStateException("support action bar should be set")

        with(actionBar) {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }

        stepPersistentWrapper = intent.getParcelableExtra(EXTRA_STEP_WRAPPER)
            ?: throw IllegalStateException("StepPersistentWrapper cannot be null")

        lessonData = intent.getParcelableExtra(EXTRA_LESSON_DATA)
            ?: throw IllegalStateException("Lesson data cannot be null")

        fullScreenCodeToolbarTitle.text = lessonData.lesson.title

        initViewPager()

        presenter = ViewModelProviders.of(this, viewModelFactory).get(StepQuizPresenter::class.java)
        presenter.onStepData(stepPersistentWrapper, lessonData)

        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<StepQuizView.State.Idle>()
        viewStateDelegate.addState<StepQuizView.State.Loading>(stepQuizProgress)
        viewStateDelegate.addState<StepQuizView.State.AttemptLoaded>(fullScreenCodeViewPager)

        val actionsListenerNew = object : CodeStepQuizFullScreenFormDelegate.ActionsListener {
            override fun onSubmitClicked() {
                syncCodeOnFullScreenExit(true)
            }

            override fun onChangeLanguageClicked() {
                val dialog = ChangeCodeLanguageDialog.newInstance()
                if (!dialog.isAdded) {
                    dialog.show(supportFragmentManager, null)
                }
            }
        }
        codeStepQuizFormFullScreenDelegate = CodeStepQuizFullScreenFormDelegate(instructionsLayout, playgroundLayout, coordinator, stepPersistentWrapper, actionsListenerNew)
    }

    override fun onStart() {
        super.onStart()
        presenter.attachView(this)
    }

    override fun onStop() {
        presenter.detachView(this)
        val reply = codeStepQuizFormFullScreenDelegate.createReply()
        if (reply is ReplyResult.Success) {
            presenter.syncReplyState(reply.reply)
        }
        super.onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean =
        when (item?.itemId) {
            android.R.id.home -> {
                syncCodeOnFullScreenExit(false)
                true
            }
            R.id.action_reset_code -> {
                val dialog = ResetCodeDialogFragment.newInstance()
                if (!dialog.isAdded) {
                    dialog.show(supportFragmentManager, null)
                }
                true
            }
            R.id.action_language_code -> {
                val dialog = ChangeCodeLanguageDialog.newInstance()
                if (!dialog.isAdded) {
                    dialog.show(supportFragmentManager, null)
                }
                true
            }
            else -> false
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.code_playground_menu, menu)
        val menuItem = menu?.findItem(R.id.action_reset_code)
        val resetString = SpannableString(getString(R.string.code_quiz_reset))
        resetString.setSpan(ForegroundColorSpan(ColorUtil.getColorArgb(R.color.new_red_color)), 0, resetString.length, 0)
        menuItem?.title = resetString
        return super.onCreateOptionsMenu(menu)
    }

    private fun initViewPager() {
        val lightFont = TypefaceUtils.load(assets, fontsProvider.provideFontPath(FontType.light))
        val regularFont = TypefaceUtils.load(assets, fontsProvider.provideFontPath(FontType.regular))

        val pagerAdapter = CodeStepQuizFullScreenPagerAdapter(this)

        fullScreenCodeViewPager.adapter = pagerAdapter
        fullScreenCodeTabs.setupWithViewPager(fullScreenCodeViewPager)
        fullScreenCodeViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {}
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}
            override fun onPageSelected(p0: Int) {
                if (p0 == 0) {
                    this@CodeStepQuizFullScreenActivity.currentFocus?.let { inputMethodService.hideSoftInputFromWindow(it.windowToken, 0) }
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

    override fun setState(state: StepQuizView.State) {
        viewStateDelegate.switchState(state)
        if (state is StepQuizView.State.AttemptLoaded) {
            codeStepQuizFormFullScreenDelegate.setState(state)
        }
    }

    override fun onBackPressed() {
        syncCodeOnFullScreenExit(false)
    }

    override fun showNetworkError() {
        Snackbar
            .make(root, R.string.no_connection, Snackbar.LENGTH_SHORT)
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }

    override fun onChangeLanguage() {
        val languages = stepPersistentWrapper.step.block?.options?.limits?.keys?.sorted()?.toTypedArray() ?: emptyArray()

        val dialog = ProgrammingLanguageChooserDialogFragment.newInstance(languages)
        if (!dialog.isAdded) {
            dialog.show(supportFragmentManager, null)
        }
    }

    override fun onLanguageChosen(programmingLanguage: String) {
        codeStepQuizFormFullScreenDelegate.onLanguageSelected(programmingLanguage)
    }

    override fun onReset() {
        codeStepQuizFormFullScreenDelegate.onResetCode()
    }

    private fun syncCodeOnFullScreenExit(isSubmittedClicked: Boolean = false) {
        val reply = codeStepQuizFormFullScreenDelegate.createReply()
        if (reply is ReplyResult.Success) {
            presenter.syncReplyState(reply.reply) {
                val data = Intent()
                data.putExtra(IS_SUBMITTED_CLICKED, isSubmittedClicked)
                setResult(Activity.RESULT_OK, data)
                finish()
            }
        }
    }

    private fun injectComponent() {
        App.component()
            .stepComponentBuilder()
            .build()
            .inject(this)
    }
}