package org.stepik.android.view.lesson.ui.activity

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.content.res.AppCompatResources
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_lesson.*
import kotlinx.android.synthetic.main.empty_login.*
import kotlinx.android.synthetic.main.error_lesson_not_found.*
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.view_centered_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.adapters.StepFragmentAdapter
import org.stepic.droid.ui.dialogs.RateAppDialogFragment
import org.stepic.droid.ui.listeners.NextMoveable
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.DeviceInfoUtil
import org.stepic.droid.util.RatingUtil
import org.stepic.droid.util.reportRateEvent
import org.stepic.droid.util.resolvers.StepTypeResolver
import org.stepik.android.domain.feedback.model.SupportEmailData
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Step
import org.stepik.android.model.Unit
import org.stepik.android.presentation.lesson.LessonPresenter
import org.stepik.android.presentation.lesson.LessonView
import org.stepik.android.view.fragment_pager.FragmentDelegateScrollStateChangeListener
import org.stepik.android.view.lesson.routing.getLessonDeepLinkData
import org.stepik.android.view.lesson.ui.delegate.LessonInfoTooltipDelegate
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class LessonActivity : FragmentActivityBase(), LessonView, NextMoveable, RateAppDialogFragment.Companion.Callback {
    companion object {
        private const val EXTRA_SECTION = "section"
        private const val EXTRA_UNIT = "unit"
        private const val EXTRA_LESSON = "lesson"
        private const val EXTRA_BACK_ANIMATION = "back_animation"

        private const val EXTRA_LAST_STEP = "last_step"

        fun createIntent(context: Context, section: Section, unit: Unit, lesson: Lesson, isNeedBackAnimation: Boolean = false): Intent =
            Intent(context, LessonActivity::class.java)
                .putExtra(EXTRA_SECTION, section)
                .putExtra(EXTRA_UNIT, unit)
                .putExtra(EXTRA_LESSON, lesson)
                .putExtra(EXTRA_BACK_ANIMATION, isNeedBackAnimation)

        fun createIntent(context: Context, lastStep: LastStep): Intent =
            Intent(context, LessonActivity::class.java)
                .putExtra(EXTRA_LAST_STEP, lastStep)
    }

    @Inject
    internal lateinit var stepTypeResolver: StepTypeResolver

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var lessonPresenter: LessonPresenter

    private lateinit var viewStateDelegate: ViewStateDelegate<LessonView.State>
    private lateinit var viewStepStateDelegate: ViewStateDelegate<LessonView.StepsState>

    private lateinit var stepsAdapter: StepFragmentAdapter

    private var infoMenuItem: MenuItem? = null
    private var isInfoMenuItemVisible: Boolean = false
        set(value) {
            field = value
            infoMenuItem?.isVisible = value
        }

    private lateinit var lessonInfoTooltipDelegate: LessonInfoTooltipDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lesson)

        if (savedInstanceState == null) {
            if (intent.getBooleanExtra(EXTRA_BACK_ANIMATION, false)) {
                overridePendingTransition(R.anim.slide_in_from_start, R.anim.slide_out_to_end)
            } else {
                overridePendingTransition(R.anim.slide_in_from_end, R.anim.slide_out_to_start)
            }
        }

        injectComponent()
        lessonPresenter = ViewModelProviders
            .of(this, viewModelFactory)
            .get(LessonPresenter::class.java)

        initCenteredToolbar(R.string.lesson_title, showHomeButton = true)

        viewStateDelegate = ViewStateDelegate()
        viewStateDelegate.addState<LessonView.State.Idle>(lessonPlaceholder)
        viewStateDelegate.addState<LessonView.State.Loading>(lessonPlaceholder)
        viewStateDelegate.addState<LessonView.State.LessonNotFound>(lessonNotFound)
        viewStateDelegate.addState<LessonView.State.EmptyLogin>(emptyLogin)
        viewStateDelegate.addState<LessonView.State.NetworkError>(errorNoConnection)
        viewStateDelegate.addState<LessonView.State.LessonLoaded>(lessonPager)

        viewStepStateDelegate = ViewStateDelegate()
        viewStepStateDelegate.addState<LessonView.StepsState.Idle>(lessonPlaceholder)
        viewStepStateDelegate.addState<LessonView.StepsState.Loading>(lessonPlaceholder)
        viewStepStateDelegate.addState<LessonView.StepsState.NetworkError>(errorNoConnection)
        viewStepStateDelegate.addState<LessonView.StepsState.EmptySteps>(emptyLesson)
        viewStepStateDelegate.addState<LessonView.StepsState.AccessDenied>(lessonNotFound)
        viewStepStateDelegate.addState<LessonView.StepsState.Loaded>(lessonPager, lessonTab)

        lessonInfoTooltipDelegate = LessonInfoTooltipDelegate(centeredToolbar)

        tryAgain.setOnClickListener { setDataToPresenter(forceUpdate = true) }
        goToCatalog.setOnClickListener { screenManager.showCatalog(this); finish() }
        authAction.setOnClickListener { screenManager.showLaunchScreen(this) }

        stepsAdapter = StepFragmentAdapter(supportFragmentManager, stepTypeResolver)
        lessonPager.adapter = stepsAdapter
        lessonPager.addOnPageChangeListener(FragmentDelegateScrollStateChangeListener(lessonPager, stepsAdapter))
        lessonPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                hideSoftKeypad()
                lessonPresenter.onStepOpened(position)
            }
        })
        lessonTab.setupWithViewPager(lessonPager, true)

        setDataToPresenter()
    }

    private fun injectComponent() {
        App.component()
            .lessonComponentBuilder()
            .build()
            .inject(this)
    }

    private fun setDataToPresenter(forceUpdate: Boolean = false) {
        val lastStep = intent.getParcelableExtra<LastStep>(EXTRA_LAST_STEP)

        val lesson = intent.getParcelableExtra<Lesson>(EXTRA_LESSON)
        val unit = intent.getParcelableExtra<Unit>(EXTRA_UNIT)
        val section = intent.getParcelableExtra<Section>(EXTRA_SECTION)

        val isFromNextLesson = intent.getBooleanExtra(EXTRA_BACK_ANIMATION, false)

        val deepLinkData = intent.getLessonDeepLinkData()

        when {
            lastStep != null ->
                lessonPresenter.onLastStep(lastStep, forceUpdate)

            deepLinkData != null ->
                lessonPresenter.onDeepLink(deepLinkData, forceUpdate)

            lesson != null && unit != null && section != null ->
                lessonPresenter.onLesson(lesson, unit, section, isFromNextLesson, forceUpdate)

            else ->
                lessonPresenter.onEmptyData()
        }
    }

    override fun onStart() {
        super.onStart()
        lessonPresenter.attachView(this)
    }

    override fun onStop() {
        lessonPresenter.detachView(this)
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.lesson_menu, menu)
        infoMenuItem = menu.findItem(R.id.lesson_menu_item_info)
        infoMenuItem?.isVisible = isInfoMenuItemVisible
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.lesson_menu_item_info -> {
                lessonPresenter.onShowLessonInfoClicked(lessonPager.currentItem)
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }

    override fun setState(state: LessonView.State) {
        viewStateDelegate.switchState(state)
        when (state) {
            is LessonView.State.LessonLoaded -> {
                viewStepStateDelegate.switchState(state.stepsState)
                centeredToolbarTitle.text = state.lessonData.lesson.title

                stepsAdapter.lessonData = state.lessonData
                stepsAdapter.items =
                    if (state.stepsState is LessonView.StepsState.Loaded) {
                        state.stepsState.stepItems
                    } else {
                        emptyList()
                    }

                invalidateTabLayout()
            }
        }

        isInfoMenuItemVisible =
            state is LessonView.State.LessonLoaded &&
            state.stepsState is LessonView.StepsState.Loaded
    }

    private fun invalidateTabLayout() {
        for (i in 0 until lessonTab.tabCount) {
            val tabIcon = AppCompatResources
                .getDrawable(this, stepsAdapter.getTabDrawable(i))
                ?.mutate()

            val tintColor = ContextCompat
                .getColor(this, stepsAdapter.getTabTint(i))

            lessonTab.getTabAt(i)?.icon = tabIcon
            tabIcon?.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
        }
    }

    override fun showStepAtPosition(position: Int) {
        lessonPager.currentItem = position
        lessonPresenter.onStepOpened(position)
    }

    override fun showLessonInfoTooltip(stepWorth: Long, lessonTimeToComplete: Long, certificateThreshold: Long) {
        lessonInfoTooltipDelegate
            .showLessonInfoTooltip(stepWorth, lessonTimeToComplete, certificateThreshold)
    }

    override fun moveNext(): Boolean {
        val itemCount = lessonPager
            .adapter
            ?.count
            ?: return false

        val isNotLastItem = lessonPager.currentItem < itemCount - 1

        if (isNotLastItem) {
            lessonPager.currentItem++
        }

        return isNotLastItem
    }

    override fun showComments(step: Step, discussionId: Long) {
        // todo: use discussion id after comments refactor
        screenManager.openComments(this, step.discussionProxy, step.id)
    }

    override fun showRateDialog() {
        val rateAppDialogFragment = RateAppDialogFragment.newInstance()
        if (!rateAppDialogFragment.isAdded) {
            analytic.reportEvent(Analytic.Rating.SHOWN)
            rateAppDialogFragment.show(supportFragmentManager, null)
        }
    }

    override fun onClickLater(starNumber: Int) {
        if (RatingUtil.isExcellent(starNumber)) {
            analytic.reportRateEvent(starNumber, Analytic.Rating.POSITIVE_LATER)
        } else {
            analytic.reportRateEvent(starNumber, Analytic.Rating.NEGATIVE_LATER)
        }
    }

    override fun onClickGooglePlay(starNumber: Int) {
        sharedPreferenceHelper.afterRateWasHandled()
        analytic.reportRateEvent(starNumber, Analytic.Rating.POSITIVE_APPSTORE)

        if (config.isAppInStore) {
            screenManager.showStoreWithApp(this)
        } else {
            setupTextFeedback()
        }
    }

    override fun onClickSupport(starNumber: Int) {
        sharedPreferenceHelper.afterRateWasHandled()
        analytic.reportRateEvent(starNumber, Analytic.Rating.NEGATIVE_EMAIL)
        setupTextFeedback()
    }

    override fun sendTextFeedback(supportEmailData: SupportEmailData) {
        screenManager.openTextFeedBack(this, supportEmailData)
    }

    private fun setupTextFeedback() {
        lessonPresenter.sendTextFeedback(
            getString(R.string.feedback_subject),
            DeviceInfoUtil.getInfosAboutDevice(this, "\n")
        )
    }
}