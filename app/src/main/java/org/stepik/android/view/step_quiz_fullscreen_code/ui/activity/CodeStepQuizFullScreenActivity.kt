package org.stepik.android.view.step_quiz_fullscreen_code.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_step_quiz_code_fullscreen.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.fonts.FontType
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.util.BackButtonHandler
import org.stepic.droid.ui.util.OnBackClickListener
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.view.base.ui.interfaces.KeyboardExtensionContainer
import org.stepik.android.view.step_quiz_fullscreen_code.ui.adapter.CodeStepQuizFullScreenAdapter
import uk.co.chrisjenx.calligraphy.TypefaceUtils
import java.lang.ref.WeakReference

class CodeStepQuizFullScreenActivity : FragmentActivityBase(), BackButtonHandler,
    KeyboardExtensionContainer {
    companion object {
        private val EXTRA_CURRENT_LANG = "current_lang_key"
        private val EXTRA_STEP_WRAPPER = "step_wrapper"
        private val EXTRA_LESSON_DATA = "lesson_data"

        fun createIntent(context: Context, lang: String, stepPersistentWrapper: StepPersistentWrapper, lessonData: LessonData): Intent =
            Intent(context, CodeStepQuizFullScreenActivity::class.java)
                .putExtra(EXTRA_CURRENT_LANG, lang)
                .putExtra(EXTRA_STEP_WRAPPER, stepPersistentWrapper)
                .putExtra(EXTRA_LESSON_DATA, lessonData)
    }

    private var onBackClickListener: WeakReference<OnBackClickListener>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step_quiz_code_fullscreen)

        setSupportActionBar(fullScreenCodeToolbar)
        val actionBar = this.supportActionBar
            ?: throw IllegalStateException("support action bar should be set")

        with(actionBar) {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }

        val currentLang: String = intent.getStringExtra(EXTRA_CURRENT_LANG)
            ?: throw IllegalStateException("Current lang cannot be null")

        val stepPersistentWrapper: StepPersistentWrapper = intent.getParcelableExtra(EXTRA_STEP_WRAPPER)
            ?: throw IllegalStateException("StepPersistentWrapper cannot be null")

        val lessonData: LessonData = intent.getParcelableExtra(EXTRA_LESSON_DATA)
            ?: throw IllegalStateException("Lesson data cannot be null")

        fullScreenCodeToolbarTitle.text = lessonData.lesson.title

        initViewPager(currentLang, stepPersistentWrapper, lessonData)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            if (fragmentBackKeyIntercept()) {
                true
            } else {
                finish()
                true
            }
        } else {
            false
        }

    override fun onBackPressed() {
        if (!fragmentBackKeyIntercept()) {
            super.onBackPressed()
        }
    }

    private fun initViewPager(currentLang: String, stepPersistentWrapper: StepPersistentWrapper, lessonData: LessonData) {
        val lightFont = TypefaceUtils.load(assets, fontsProvider.provideFontPath(FontType.light))
        val regularFont = TypefaceUtils.load(assets, fontsProvider.provideFontPath(FontType.regular))

        val codePagerAdapter = CodeStepQuizFullScreenAdapter(this, currentLang, supportFragmentManager, stepPersistentWrapper, lessonData)

        fullScreenCodeViewPager.adapter = codePagerAdapter
        fullScreenCodeTabs.setupWithViewPager(fullScreenCodeViewPager)
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
    }

    override fun setBackClickListener(onBackClickListener: OnBackClickListener) {
        this.onBackClickListener = WeakReference(onBackClickListener)
    }

    override fun removeBackClickListener(onBackClickListener: OnBackClickListener) {
        this.onBackClickListener = null
    }

    private fun fragmentBackKeyIntercept(): Boolean =
        onBackClickListener?.get()?.onBackClick() ?: false

    override fun getKeyboardExtensionViewContainer(): ViewGroup =
        coordinator
}