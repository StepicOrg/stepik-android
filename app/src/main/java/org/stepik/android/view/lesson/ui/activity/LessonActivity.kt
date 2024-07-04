package org.stepik.android.view.lesson.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_lesson.*
import kotlinx.android.synthetic.main.empty_login.*
import kotlinx.android.synthetic.main.error_lesson_is_exam.*
import kotlinx.android.synthetic.main.error_lesson_not_found.*
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.layout_step_tab_icon.view.*
import kotlinx.android.synthetic.main.view_subtitled_toolbar.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.adapters.StepFragmentAdapter
import org.stepic.droid.ui.dialogs.TimeIntervalPickerDialogFragment
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.ui.util.snackbar
import org.stepic.droid.util.DeviceInfoUtil
import org.stepic.droid.util.REQUEST_NOTIFICATION_PERMISSION
import org.stepic.droid.util.RatingUtil
import org.stepic.droid.util.isNotificationPermissionGranted
import org.stepic.droid.util.reportRateEvent
import org.stepic.droid.util.requestMultiplePermissions
import org.stepic.droid.util.resolvers.StepTypeResolver
import org.stepik.android.domain.feedback.model.SupportEmailData
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step.model.StepNavigationDirection
import org.stepik.android.model.Lesson
import org.stepik.android.model.Section
import org.stepik.android.model.Step
import org.stepik.android.model.Unit
import org.stepik.android.model.comments.DiscussionThread
import org.stepik.android.presentation.lesson.LessonPresenter
import org.stepik.android.presentation.lesson.LessonView
import org.stepik.android.view.app_rating.ui.dialog.RateAppDialog
import org.stepik.android.view.course.routing.CourseDeepLinkBuilder
import org.stepik.android.view.course.routing.CourseScreenTab
import org.stepik.android.view.fragment_pager.FragmentDelegateScrollStateChangeListener
import org.stepik.android.view.lesson.routing.getLessonDeepLinkData
import org.stepik.android.view.lesson.ui.delegate.LessonInfoTooltipDelegate
import org.stepik.android.view.lesson.ui.interfaces.Moveable
import org.stepik.android.view.lesson.ui.interfaces.Playable
import org.stepik.android.view.lesson.ui.mapper.LessonTitleMapper
import org.stepik.android.view.magic_links.ui.dialog.MagicLinkDialogFragment
import org.stepik.android.view.streak.ui.dialog.StreakNotificationDialogFragment
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.hideKeyboard
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

class LessonActivity : FragmentActivityBase(), LessonView,
    Moveable,
    RateAppDialog.Companion.Callback,
    TimeIntervalPickerDialogFragment.Companion.Callback,
    StreakNotificationDialogFragment.Callback {
    companion object {
        private const val EXTRA_SECTION = "section"
        private const val EXTRA_UNIT = "unit"
        private const val EXTRA_LESSON = "lesson"
        private const val EXTRA_BACK_ANIMATION = "back_animation"
        private const val EXTRA_AUTOPLAY = "autoplay"

        private const val EXTRA_LAST_STEP = "last_step"

        private const val EXTRA_TRIAL_LESSON_ID = "trial_lesson_id"
        private const val EXTRA_TRIAL_UNIT_ID = "trial_unit_id"

        private const val EXTRA_LESSON_DATA = "lesson_data"

        const val EXTRA_MOVE_STEP_NAVIGATION_DIRECTION = "move_step_navigation_direction"

        fun createIntent(context: Context, section: Section, unit: Unit, lesson: Lesson, isNeedBackAnimation: Boolean = false, isAutoplayEnabled: Boolean = false): Intent =
            Intent(context, LessonActivity::class.java)
                .putExtra(EXTRA_SECTION, section)
                .putExtra(EXTRA_UNIT, unit)
                .putExtra(EXTRA_LESSON, lesson)
                .putExtra(EXTRA_BACK_ANIMATION, isNeedBackAnimation)
                .putExtra(EXTRA_AUTOPLAY, isAutoplayEnabled)

        fun createIntent(context: Context, lessonData: LessonData): Intent =
            Intent(context, LessonActivity::class.java)
                .putExtra(EXTRA_LESSON_DATA, lessonData)

        fun createIntent(context: Context, lastStep: LastStep): Intent =
            Intent(context, LessonActivity::class.java)
                .putExtra(EXTRA_LAST_STEP, lastStep)

        fun createIntent(context: Context, trialLessonId: Long, trialUnitId: Long): Intent =
            Intent(context, LessonActivity::class.java)
                .putExtra(EXTRA_TRIAL_LESSON_ID, trialLessonId)
                .putExtra(EXTRA_TRIAL_UNIT_ID, trialUnitId)
    }

    @Inject
    internal lateinit var stepTypeResolver: StepTypeResolver

    @Inject
    internal lateinit var courseDeepLinkBuilder: CourseDeepLinkBuilder

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var lessonTitleMapper: LessonTitleMapper

    private val lessonPresenter: LessonPresenter by viewModels { viewModelFactory }

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

            // handle wrong deeplink interceptions
            intent.data?.let { uri -> screenManager.redirectToWebBrowserIfNeeded(this, uri) }
        }

        injectComponent()

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
        viewStepStateDelegate.addState<LessonView.StepsState.Exam>(lessonIsExam)
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
                currentFocus?.hideKeyboard()
                lessonPresenter.onStepOpened(position)
                centeredToolbarSubtitle.text = getString(
                    R.string.lesson_step_counter, position + 1,
                    stepsAdapter.items.size
                )
                invalidateOptionsMenu()
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

        val trialLessonId = intent.getLongExtra(EXTRA_TRIAL_LESSON_ID, 0L)
        val trialUnitId = intent.getLongExtra(EXTRA_TRIAL_UNIT_ID, 0L)

        val lessonData = intent.getParcelableExtra<LessonData>(EXTRA_LESSON_DATA)

        when {
            lastStep != null ->
                lessonPresenter.onLastStep(lastStep, forceUpdate)

            deepLinkData != null ->
                lessonPresenter.onDeepLink(deepLinkData, forceUpdate)

            lesson != null && unit != null && section != null ->
                lessonPresenter.onLesson(lesson, unit, section, isFromNextLesson, forceUpdate)

            trialLessonId != 0L ->
                lessonPresenter.onTrialLesson(trialLessonId, trialUnitId, forceUpdate)

            lessonData != null ->
                lessonPresenter.onLessonData(lessonData)

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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent != null) {
            val stepNavigationDirection = intent.getIntExtra(EXTRA_MOVE_STEP_NAVIGATION_DIRECTION, -1)
            when {
                stepNavigationDirection != -1 -> {
                    lessonPager.post { (stepsAdapter.activeFragments[lessonPager.currentItem] as? Moveable)?.move(isAutoplayEnabled = true, stepNavigationDirection = StepNavigationDirection.values()[stepNavigationDirection]) }
                    intent.removeExtra(EXTRA_MOVE_STEP_NAVIGATION_DIRECTION)
                }

                intent.data != null -> {
                    finish()
                    startActivity(intent)
                }
            }
        }
    }

    override fun setState(state: LessonView.State) {
        viewStateDelegate.switchState(state)
        when (state) {
            is LessonView.State.LessonLoaded -> {
                viewStepStateDelegate.switchState(state.stepsState)
                setupToolbarTitle(state.lessonData)
                if (centeredToolbarSubtitle.text.isEmpty()) {
                    centeredToolbarSubtitle.text = getString(
                        R.string.lesson_step_counter, state.lessonData.stepPosition + 1,
                        state.lessonData.lesson.steps.size
                    )
                }

                stepsAdapter.lessonData = state.lessonData
                if (state.stepsState is LessonView.StepsState.Loaded) {
                    stepsAdapter.items = state.stepsState.stepItems

                    if (intent.getBooleanExtra(EXTRA_AUTOPLAY, false)) {
                        lessonPager.post { playCurrentStep() }
                        intent.removeExtra(EXTRA_AUTOPLAY)
                    }

                    val stepNavigationDirectionExtra = intent.getIntExtra(EXTRA_MOVE_STEP_NAVIGATION_DIRECTION, -1)
                    if (stepNavigationDirectionExtra != -1) {
                        lessonPager.post {
                            (stepsAdapter.activeFragments[lessonPager.currentItem] as? Moveable)
                                ?.move(isAutoplayEnabled = true, stepNavigationDirection = StepNavigationDirection.values()[stepNavigationDirectionExtra])
                        }
                        intent.removeExtra(EXTRA_MOVE_STEP_NAVIGATION_DIRECTION)
                    }
                } else {
                    if (state.stepsState is LessonView.StepsState.Exam) {
                        errorLessonIsExamAction.setOnClickListener {
                            val url = courseDeepLinkBuilder
                                .createCourseLink(state.stepsState.courseId, CourseScreenTab.SYLLABUS)

                            MagicLinkDialogFragment
                                .newInstance(url)
                                .showIfNotExists(supportFragmentManager, MagicLinkDialogFragment.TAG)
                        }
                    }
                    stepsAdapter.items = emptyList()
                }

                centeredToolbarSubtitle.isVisible = stepsAdapter.items.isNotEmpty()
                invalidateTabLayout()
            }
        }

        isInfoMenuItemVisible =
            state is LessonView.State.LessonLoaded &&
            state.stepsState is LessonView.StepsState.Loaded
    }

    private fun setupToolbarTitle(lessonData: LessonData) {
        centeredToolbarTitle.text =
            lessonTitleMapper.mapToLessonTitle(this, lessonData)
    }

    private fun invalidateTabLayout() {
        for (i in 0 until lessonTab.tabCount) {
            val tabFrames = stepsAdapter.getTabDrawable(i)

            val item = stepsAdapter.items[i]
            val isPassed =
                item.stepWrapper.step.instructionType != null &&
                        item.reviewSession?.isFinished == true ||
                item.stepWrapper.step.instructionType == null &&
                        item.assignmentProgress?.isPassed ?: item.stepProgress?.isPassed ?: false

            @DrawableRes
            val tabIconResource = if (!isPassed) {
                tabFrames.first
            } else {
                tabFrames.second
            }

            if (lessonTab.getTabAt(i)?.customView == null) {
                lessonTab.getTabAt(i)?.customView = View.inflate(this, R.layout.layout_step_tab_icon, null)
            }

            lessonTab.getTabAt(i)?.customView?.tabIconDrawable?.apply {
                setImageResource(tabIconResource)
                isEnabled = isPassed
            }
        }
    }

    override fun showStepAtPosition(position: Int) {
        lessonPager.currentItem = position
        lessonPresenter.onStepOpened(position)
    }

    override fun showLessonInfoTooltip(stepScore: Float, stepCost: Long, lessonTimeToComplete: Long, certificateThreshold: Long, isExam: Boolean) {
        lessonInfoTooltipDelegate
            .showLessonInfoTooltip(stepScore, stepCost, lessonTimeToComplete, certificateThreshold, isExam)
    }

    override fun move(
        isAutoplayEnabled: Boolean,
        stepNavigationDirection: StepNavigationDirection
    ): Boolean {
        val itemCount = lessonPager
            .adapter
            ?.count
            ?: return false

        return if (stepNavigationDirection == StepNavigationDirection.NEXT) {
            val isNotLastItem = lessonPager.currentItem < itemCount - 1
            if (isNotLastItem) {
                lessonPager.currentItem++
                if (isAutoplayEnabled) {
                    playCurrentStep()
                }
            }
            isNotLastItem
        } else {
            val isNotFirstItem = lessonPager.currentItem > 0
            if (isNotFirstItem) {
                lessonPager.currentItem--
                if (isAutoplayEnabled) {
                    playCurrentStep()
                }
            }
            isNotFirstItem
        }
    }

    private fun playCurrentStep() {
        (stepsAdapter.activeFragments[lessonPager.currentItem] as? Playable)
            ?.play()
    }

    override fun showComments(step: Step, discussionId: Long, discussionThread: DiscussionThread?, isTeacher: Boolean) {
        if (discussionThread != null) {
            analytic.reportAmplitudeEvent(
                AmplitudeAnalytic.Discussions.SCREEN_OPENED,
                mapOf(AmplitudeAnalytic.Discussions.Params.SOURCE to AmplitudeAnalytic.Discussions.Values.DISCUSSION)
            )
            screenManager.openComments(this, discussionThread, step, discussionId, false, isTeacher)
        } else {
            analytic.reportEvent(Analytic.Screens.OPEN_COMMENT_NOT_AVAILABLE)
            lessonPager.snackbar(messageRes = R.string.comment_disabled)
        }
    }

    override fun showRateDialog() {
        analytic.reportEvent(Analytic.Rating.SHOWN)
        RateAppDialog
            .newInstance()
            .showIfNotExists(supportFragmentManager, RateAppDialog.TAG)
    }

    override fun showStreakDialog(streakDays: Int) {
        val description =
            if (streakDays > 0) {
                analytic.reportEvent(Analytic.Streak.SHOW_DIALOG_UNDEFINED_STREAKS, streakDays.toString())
                resources.getQuantityString(R.plurals.streak_description, streakDays, streakDays)
            } else {
                analytic.reportEvent(Analytic.Streak.SHOW_DIALOG_POSITIVE_STREAKS, streakDays.toString())
                getString(R.string.streak_description_not_positive)
            }

        analytic.reportEvent(Analytic.Streak.SHOWN_MATERIAL_DIALOG)

        StreakNotificationDialogFragment
            .newInstance(
                title = getString(R.string.streak_dialog_title),
                message = description,
                positiveEvent = Analytic.Streak.POSITIVE_MATERIAL_DIALOG
            )
            .showIfNotExists(supportFragmentManager, StreakNotificationDialogFragment.TAG)
    }

    override fun onClickLater(starNumber: Int) {
        if (RatingUtil.isExcellent(starNumber)) {
            analytic.reportRateEvent(starNumber, Analytic.Rating.POSITIVE_LATER)
        } else {
            analytic.reportRateEvent(starNumber, Analytic.Rating.NEGATIVE_LATER)
        }
    }

    override fun onClickGooglePlay(starNumber: Int) {
        lessonPresenter.onAppRateShow()
        analytic.reportRateEvent(starNumber, Analytic.Rating.POSITIVE_APPSTORE)

        if (config.isAppInStore) {
            screenManager.showStoreWithApp(this)
        } else {
            setupTextFeedback()
        }
    }

    override fun onClickSupport(starNumber: Int) {
        lessonPresenter.onAppRateShow()
        analytic.reportRateEvent(starNumber, Analytic.Rating.NEGATIVE_EMAIL)
        setupTextFeedback()
    }

    override fun sendTextFeedback(supportEmailData: SupportEmailData) {
        screenManager.openTextFeedBack(this, supportEmailData)
    }

    override fun onTimeIntervalPicked(chosenInterval: Int) {
        lessonPresenter.setStreakTime(chosenInterval)
        analytic.reportEvent(Analytic.Streak.CHOOSE_INTERVAL, chosenInterval.toString())
        lessonPager.snackbar(messageRes = R.string.streak_notification_enabled_successfully, length = Snackbar.LENGTH_LONG)
    }

    private fun setupTextFeedback() {
        lessonPresenter.sendTextFeedback(
            getString(R.string.feedback_subject),
            DeviceInfoUtil.getInfosAboutDevice(this, "\n")
        )
    }

    @SuppressLint("NewApi") // Suppressed, because isNotificationPermissionGranted() returns true for less that Build.VERSION_CODES.TIRAMISU
    override fun onStreakNotificationDialogAccepted() {
        if (isNotificationPermissionGranted()) {
            TimeIntervalPickerDialogFragment
                .newInstance()
                .showIfNotExists(supportFragmentManager, TimeIntervalPickerDialogFragment.TAG)
        } else {
            requestNotificationPermission()
        }
    }

    override fun onStreakNotificationDialogCancelled() {
        analytic.reportEvent(Analytic.Streak.NEGATIVE_MATERIAL_DIALOG)
        lessonPager.snackbar(messageRes = R.string.streak_notification_canceled, length = Snackbar.LENGTH_LONG)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_NOTIFICATION_PERMISSION -> {
                val deniedPermissionIndex = grantResults
                    .indexOf(PackageManager.PERMISSION_DENIED)

                if (deniedPermissionIndex != -1) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[deniedPermissionIndex])) {
                        lessonPager.snackbar(messageRes = R.string.notification_permission_error)
                    }
                } else {
                    TimeIntervalPickerDialogFragment
                        .newInstance()
                        .showIfNotExists(supportFragmentManager, TimeIntervalPickerDialogFragment.TAG)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        val permissions = listOf(Manifest.permission.POST_NOTIFICATIONS)
        requestMultiplePermissions(permissions, REQUEST_NOTIFICATION_PERMISSION)
    }
}