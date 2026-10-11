package org.stepik.android.view.course.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import dev.androidbroadcast.vbpd.viewBinding
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import org.stepic.droid.databinding.ActivityCourseBinding
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.experiments.CoursePurchaseWebviewSplitTest
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.dialogs.UnauthorizedDialogFragment
import org.stepic.droid.ui.util.snackbar
import org.stepic.droid.util.ProgressHelper
import org.stepik.android.domain.base.analytic.BUNDLEABLE_ANALYTIC_EVENT
import org.stepik.android.domain.base.analytic.toAnalyticEvent
import org.stepik.android.domain.course.analytic.CourseJoinedEvent
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course_news.analytic.CourseNewsScreenOpenedAnalyticEvent
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.domain.course_purchase.analytic.CoursePurchaseSource
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course
import org.stepik.android.presentation.course.CoursePresenter
import org.stepik.android.presentation.course.CourseView
import org.stepik.android.presentation.course.model.EnrollmentError
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData
import org.stepik.android.presentation.user_courses.model.UserCourseAction
import org.stepik.android.presentation.wishlist.model.WishlistAction
import org.stepik.android.view.course.routing.CourseDeepLinkBuilder
import org.stepik.android.view.course.routing.CourseScreenTab
import org.stepik.android.view.course.routing.getCourseIdFromDeepLink
import org.stepik.android.view.course.routing.getCourseTabFromDeepLink
import org.stepik.android.view.course.routing.getPromoCodeFromDeepLink
import org.stepik.android.view.course.ui.adapter.CoursePagerAdapter
import org.stepik.android.view.course.ui.delegates.CourseHeaderDelegate
import org.stepik.android.view.course_content.ui.fragment.CourseContentFragment
import org.stepik.android.view.course_news.ui.fragment.CourseNewsFragment
import org.stepik.android.view.course_purchase.ui.dialog.CoursePurchaseBottomSheetDialogFragment
import org.stepik.android.view.course_reviews.ui.fragment.CourseReviewsFragment
import org.stepik.android.view.course_search.dialog.CourseSearchDialogFragment
import org.stepik.android.view.fragment_pager.FragmentDelegateScrollStateChangeListener
import org.stepik.android.view.in_app_web_view.ui.dialog.InAppWebViewDialogFragment
import org.stepik.android.view.injection.course.CourseHeaderDelegateFactory
import org.stepik.android.view.magic_links.ui.dialog.MagicLinkDialogFragment
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import javax.inject.Inject

class CourseActivity :
    FragmentActivityBase(),
    CourseView,
    InAppWebViewDialogFragment.Callback,
    MagicLinkDialogFragment.Callback,
    CoursePurchaseBottomSheetDialogFragment.Callback {
    companion object {
        private const val EXTRA_COURSE = "course"
        private const val EXTRA_COURSE_ID = "course_id"
        private const val EXTRA_AUTO_ENROLL = "auto_enroll"
        private const val EXTRA_TAB = "tab"
        private const val EXTRA_SOURCE = "source"
        private const val EXTRA_DEEPLINK_PROMO_CODE = "deeplink_promo_code"
        private const val EXTRA_CONTINUE_LEARNING = "continue_learning"

        private const val NO_ID = -1L
        private const val NO_TITLE = ""

        private const val UNAUTHORIZED_DIALOG_TAG = "unauthorized_dialog"

        private const val QUERY_PARAM_PROMO = "promo"

        fun createIntent(context: Context, course: Course, source: CourseViewSource, autoEnroll: Boolean = false, continueLearning: Boolean = false, tab: CourseScreenTab = CourseScreenTab.INFO): Intent =
            Intent(context, CourseActivity::class.java)
                .putExtra(EXTRA_COURSE, course)
                .putExtra(EXTRA_SOURCE, source)
                .putExtra(EXTRA_AUTO_ENROLL, autoEnroll)
                .putExtra(EXTRA_CONTINUE_LEARNING, continueLearning)
                .putExtra(EXTRA_TAB, tab.ordinal)

        fun createIntent(context: Context, courseId: Long, source: CourseViewSource, tab: CourseScreenTab = CourseScreenTab.INFO): Intent =
            Intent(context, CourseActivity::class.java)
                .putExtra(EXTRA_COURSE_ID, courseId)
                .putExtra(EXTRA_SOURCE, source)
                .putExtra(EXTRA_TAB, tab.ordinal)

        fun createIntent(context: Context, courseId: Long, source: CourseViewSource, tab: CourseScreenTab = CourseScreenTab.INFO, deeplinkPromoCode: DeeplinkPromoCode): Intent =
            Intent(context, CourseActivity::class.java)
                .putExtra(EXTRA_COURSE_ID, courseId)
                .putExtra(EXTRA_SOURCE, source)
                .putExtra(EXTRA_TAB, tab.ordinal)
                .putExtra(EXTRA_DEEPLINK_PROMO_CODE, deeplinkPromoCode)

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    private var courseId: Long = NO_ID
    private var courseTitle: String = NO_TITLE
    private val analyticsOnPageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(page: Int) {
            when (coursePagerAdapter.getItem(page)) {
                is CourseReviewsFragment ->
                    analytic
                        .reportAmplitudeEvent(
                            AmplitudeAnalytic.CourseReview.SCREEN_OPENED,
                            mapOf(AmplitudeAnalytic.CourseReview.Params.COURSE to courseId.toString())
                        )

                is CourseNewsFragment ->
                    analytic.report(CourseNewsScreenOpenedAnalyticEvent(courseId, courseTitle))

                is CourseContentFragment ->
                    analytic
                        .reportAmplitudeEvent(
                            AmplitudeAnalytic.Sections.SCREEN_OPENED,
                            mapOf(AmplitudeAnalytic.Sections.Params.COURSE to courseId.toString())
                        )
            }
        }
    }
    private lateinit var coursePagerAdapter: CoursePagerAdapter
    private val coursePresenter: CoursePresenter by viewModels { viewModelFactory }
    private lateinit var courseHeaderDelegate: CourseHeaderDelegate

    private var unauthorizedDialogFragment: DialogFragment? = null
    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

    private val viewStateDelegate =
        ViewStateDelegate<CourseView.State>()

    private var viewPagerScrollState: Int =
        ViewPager.SCROLL_STATE_IDLE

    private val courseBinding: ActivityCourseBinding by viewBinding(ActivityCourseBinding::bind)

    private var isInSwipeableViewState = false

    private var hasSavedInstanceState: Boolean = false

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

    @Inject
    internal lateinit var coursePurchaseWebviewSplitTest: CoursePurchaseWebviewSplitTest

    @Inject
    internal lateinit var courseDeeplinkBuilder: CourseDeepLinkBuilder

    @Inject
    internal lateinit var courseHeaderDelegateFactory: CourseHeaderDelegateFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course)

        setSupportActionBar(courseBinding.courseToolbar)
        val actionBar = this.supportActionBar
            ?: throw IllegalStateException("support action bar should be set")

        with(actionBar) {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }

        val course: Course? = intent.getParcelableExtra(EXTRA_COURSE)
        val deepLinkCourseId = intent.getCourseIdFromDeepLink()

        if (savedInstanceState == null && deepLinkCourseId != null) {
            analytic.reportEvent(Analytic.DeepLink.USER_OPEN_COURSE_DETAIL_LINK, deepLinkCourseId.toString())
            analytic.reportEvent(Analytic.DeepLink.USER_OPEN_LINK_GENERAL)

            // handle wrong deeplink interceptions
            intent.data?.let { uri -> screenManager.redirectToWebBrowserIfNeeded(this, uri) }
        }

        if (course != null) {
            courseTitle = course.title.orEmpty()
            courseBinding.courseToolbarTitle.text = course.title
        }

        courseId = intent.getLongExtra(EXTRA_COURSE_ID, NO_ID)
            .takeIf { it != NO_ID }
            ?: course?.id
                    ?: deepLinkCourseId
                    ?: NO_ID

        injectComponent(courseId)

        if (savedInstanceState == null) {
            val analyticEvent = intent
                .getBundleExtra(BUNDLEABLE_ANALYTIC_EVENT)
                ?.toAnalyticEvent()

            if (analyticEvent != null) {
                analytic.report(analyticEvent)
            }
        }

        val courseViewSource = (intent.getSerializableExtra(EXTRA_SOURCE) as? CourseViewSource)
            ?: intent.getCourseIdFromDeepLink()?.let { CourseViewSource.DeepLink(intent?.dataString ?: "") }
            ?: CourseViewSource.Unknown

        courseHeaderDelegate =
            courseHeaderDelegateFactory
                .create(
                    courseActivity = this,
                    courseBinding = courseBinding,
                    coursePresenter = coursePresenter,
                    courseViewSource = courseViewSource,
                    isAuthorized = sharedPreferenceHelper.authResponseFromStore != null,
                    mustShowCourseRevenue = firebaseRemoteConfig.getBoolean(RemoteConfig.IS_COURSE_REVENUE_AVAILABLE_ANDROID) && course?.actions?.viewRevenue?.enabled == true,
                    showCourseRevenueAction = { screenManager.showCourseRevenue(this, courseId, course?.title) },
                    onSubmissionCountClicked = {
                        screenManager.showCachedAttempts(this, courseId)
                    },
                    isLocalSubmissionsEnabled = firebaseRemoteConfig[RemoteConfig.IS_LOCAL_SUBMISSIONS_ENABLED].asBoolean(),
                    showCourseSearchAction = {
                        CourseSearchDialogFragment
                            .newInstance(courseId, course?.title.orEmpty())
                            .showIfNotExists(supportFragmentManager, CourseSearchDialogFragment.TAG)
                    },
                    coursePurchaseFlowAction = { coursePurchaseData, isNeedRestoreMessage ->
                        showCoursePurchaseBottomSheetDialogFragment(coursePurchaseData, isNeedRestoreMessage)
                    }
                )

        initViewPager(courseId, course?.title.toString())
        initViewStateDelegate()

        hasSavedInstanceState = savedInstanceState != null

        setDataToPresenter(courseViewSource)

        courseBinding.courseSwipeRefresh.setOnRefreshListener {
            setDataToPresenter(courseViewSource, forceUpdate = true)
        }
        courseBinding.errorNoConnection.tryAgain.setOnClickListener { setDataToPresenter(courseViewSource, forceUpdate = true) }
        courseBinding.courseEmpty.goToCatalog.setOnClickListener {
            screenManager.showCatalog(this)
            finish()
        }
    }

    private fun setDataToPresenter(courseViewSource: CourseViewSource, forceUpdate: Boolean = false) {
        val course: Course? = intent.getParcelableExtra(EXTRA_COURSE)

        if (course != null) {
            coursePresenter.onCourse(course, courseViewSource, forceUpdate)
        } else {
            /**
             * Fallback to DeeplinkPromoCode from LessonDemoCompleteBottomSheetDialogFragment
             */
            coursePresenter.onCourseId(courseId, courseViewSource, intent.getPromoCodeFromDeepLink() ?: intent.getParcelableExtra<DeeplinkPromoCode>(EXTRA_DEEPLINK_PROMO_CODE)?.name, forceUpdate)
        }
    }

    private fun injectComponent(courseId: Long) {
        App.componentManager()
            .courseComponent(courseId)
            .coursePresentationComponentBuilder()
            .build()
            .inject(this)
    }

    private fun releaseComponent(courseId: Long) {
        App.componentManager()
            .releaseCourseComponent(courseId)
    }

    override fun onStart() {
        super.onStart()
        coursePresenter.attachView(this)
    }

    override fun onResume() {
        super.onResume()
        if (coursePurchaseWebviewSplitTest.currentGroup != CoursePurchaseWebviewSplitTest.Group.InAppWebview) {
            coursePresenter.handleCoursePurchasePressed()
        }
        courseBinding.coursePager.addOnPageChangeListener(analyticsOnPageChangeListener)
        if (!hasSavedInstanceState) {
            setCurrentTab()
        }
    }

    override fun onPause() {
        courseBinding.coursePager.removeOnPageChangeListener(analyticsOnPageChangeListener)
        super.onPause()
    }

    override fun onStop() {
        coursePresenter.detachView(this)
        super.onStop()
    }

    private fun setCurrentTab() {
        val tab = CourseScreenTab
            .values()
            .getOrNull(intent.getIntExtra(EXTRA_TAB, -1))
            ?: intent.getCourseTabFromDeepLink()

        courseBinding.coursePager.currentItem =
            when (tab) {
                CourseScreenTab.REVIEWS -> 1
                CourseScreenTab.NEWS -> 2
                CourseScreenTab.SYLLABUS -> 3
                else -> 0
            }
        if (courseBinding.coursePager.currentItem == 0) {
            analyticsOnPageChangeListener.onPageSelected(0)
        }
    }

    private fun initViewPager(courseId: Long, courseTitle: String) {
        coursePagerAdapter = CoursePagerAdapter(courseId, courseTitle, this, supportFragmentManager)
        courseBinding.coursePager.adapter = coursePagerAdapter
        val onPageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageScrollStateChanged(scrollState: Int) {
                viewPagerScrollState = scrollState
                resolveSwipeRefreshState()
            }
        }
        courseBinding.coursePager.addOnPageChangeListener(FragmentDelegateScrollStateChangeListener(courseBinding.coursePager, coursePagerAdapter))
        courseBinding.coursePager.addOnPageChangeListener(onPageChangeListener)

        courseBinding.courseTabs.setupWithViewPager(courseBinding.coursePager)
    }

    private fun initViewStateDelegate() {
        viewStateDelegate.addState<CourseView.State.EmptyCourse>(courseBinding.courseEmpty.root)
        viewStateDelegate.addState<CourseView.State.NetworkError>(courseBinding.errorNoConnection.root)
        viewStateDelegate.addState<CourseView.State.CourseLoaded>(courseBinding.headerCourse.root, courseBinding.courseTabs, courseBinding.coursePager)
        viewStateDelegate.addState<CourseView.State.BlockingLoading>(courseBinding.headerCourse.root, courseBinding.courseTabs, courseBinding.coursePager)
        viewStateDelegate.addState<CourseView.State.Loading>(courseBinding.headerCoursePlaceholder.root, courseBinding.courseTabs, courseBinding.coursePager)
        viewStateDelegate.addState<CourseView.State.Idle>(courseBinding.headerCoursePlaceholder.root, courseBinding.courseTabs, courseBinding.coursePager)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.course_activity_menu, menu)
        courseHeaderDelegate.onOptionsMenuCreated(menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            courseHeaderDelegate.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
        }

    override fun applyTransitionPrev() {
        // no-op
    }

    private fun resolveSwipeRefreshState() {
        courseBinding.courseSwipeRefresh.isEnabled =
            viewPagerScrollState == ViewPager.SCROLL_STATE_IDLE &&
                    isInSwipeableViewState
    }

    override fun setState(state: CourseView.State) {
        courseBinding.courseSwipeRefresh.isRefreshing = false
        isInSwipeableViewState = (state is CourseView.State.CourseLoaded || state is CourseView.State.NetworkError)
        resolveSwipeRefreshState()

        when (state) {
            is CourseView.State.CourseLoaded -> {
                courseHeaderDelegate.courseHeaderData = state.courseHeaderData

                if (intent.getBooleanExtra(EXTRA_AUTO_ENROLL, false)) {
                    intent.removeExtra(EXTRA_AUTO_ENROLL)
                    coursePresenter.autoEnroll()

                    analytic.report(
                        CourseJoinedEvent(
                            CourseJoinedEvent.SOURCE_PREVIEW,
                            state.courseHeaderData.course,
                            state.courseHeaderData.course.isInWishlist
                        )
                    )
                }

                if (intent.getBooleanExtra(EXTRA_CONTINUE_LEARNING, false)) {
                    intent.removeExtra(EXTRA_CONTINUE_LEARNING)
                    coursePresenter.continueLearning()
                }

                /**
                 * Obtain promo code from LessonDemoCompleteBottomSheetDialog
                 */
                val deeplinkPromoCode = intent.getParcelableExtra<DeeplinkPromoCode>(EXTRA_DEEPLINK_PROMO_CODE)
                if (deeplinkPromoCode != null) {
                    intent.removeExtra(EXTRA_DEEPLINK_PROMO_CODE)
                    val queryParams = if (deeplinkPromoCode != DeeplinkPromoCode.EMPTY) {
                        mapOf(QUERY_PARAM_PROMO to listOf(deeplinkPromoCode.name))
                    } else {
                        null
                    }
                    coursePresenter.openCoursePurchaseInWeb(queryParams)
                }

                ProgressHelper.dismiss(supportFragmentManager, LoadingProgressDialogFragment.TAG)
            }

            is CourseView.State.BlockingLoading -> {
                courseHeaderDelegate.courseHeaderData = state.courseHeaderData
                ProgressHelper.activate(progressDialogFragment, supportFragmentManager, LoadingProgressDialogFragment.TAG)
            }

            else -> Unit
        }
        viewStateDelegate.switchState(state)
        invalidateOptionsMenu()
    }

    override fun showEmptyAuthDialog(course: Course) {
        if (unauthorizedDialogFragment?.isAdded != true) {
            unauthorizedDialogFragment = UnauthorizedDialogFragment.newInstance(course)
            unauthorizedDialogFragment?.show(supportFragmentManager, UNAUTHORIZED_DIALOG_TAG)
        }
    }

    override fun showEnrollmentError(errorType: EnrollmentError) {
        @StringRes
        val errorMessage =
            when (errorType) {
                EnrollmentError.NO_CONNECTION ->
                    R.string.course_error_enroll

                EnrollmentError.FORBIDDEN ->
                    R.string.join_course_web_exception

                EnrollmentError.UNAUTHORIZED ->
                    R.string.unauthorization_detail

                EnrollmentError.BILLING_ERROR ->
                    R.string.course_purchase_billing_error

                EnrollmentError.BILLING_CANCELLED ->
                    R.string.course_purchase_billing_cancelled

                EnrollmentError.BILLING_NOT_AVAILABLE ->
                    R.string.course_purchase_billing_not_available

                EnrollmentError.COURSE_ALREADY_OWNED ->
                    R.string.course_purchase_already_owned

                EnrollmentError.BILLING_NO_PURCHASES_TO_RESTORE ->
                    R.string.course_purchase_billing_no_purchases_to_restore
            }

        courseBinding.coursePager.snackbar(messageRes = errorMessage)
    }

    override fun shareCourse(course: Course) {
        startActivity(shareHelper.getIntentForCourseSharing(course))
    }

    override fun showCourseShareTooltip() {
        courseHeaderDelegate.showCourseShareTooltip()
    }

    override fun showSaveUserCourseSuccess(userCourseAction: UserCourseAction) {
        @StringRes
        val successMessage =
            when (userCourseAction) {
                UserCourseAction.ADD_FAVORITE ->
                    R.string.course_action_favorites_add_success

                UserCourseAction.REMOVE_FAVORITE ->
                    R.string.course_action_favorites_remove_success

                UserCourseAction.ADD_ARCHIVE ->
                    R.string.course_action_archive_add_success

                UserCourseAction.REMOVE_ARCHIVE ->
                    R.string.course_action_archive_remove_success
            }
        courseBinding.coursePager.snackbar(messageRes = successMessage)
    }

    override fun showSaveUserCourseError(userCourseAction: UserCourseAction) {
        @StringRes
        val errorMessage =
            when (userCourseAction) {
                UserCourseAction.ADD_FAVORITE ->
                    R.string.course_action_favorites_add_failure

                UserCourseAction.REMOVE_FAVORITE ->
                    R.string.course_action_favorites_remove_failure

                UserCourseAction.ADD_ARCHIVE ->
                    R.string.course_action_archive_add_failure

                UserCourseAction.REMOVE_ARCHIVE ->
                    R.string.course_action_archive_remove_failure
            }
        courseBinding.coursePager.snackbar(messageRes = errorMessage)
    }

    override fun showWishlistActionSuccess(wishlistAction: WishlistAction) {
        @StringRes
        val successMessage =
            if (wishlistAction == WishlistAction.ADD) {
                R.string.wishlist_action_add_success
            } else {
                R.string.wishlist_action_remove_success
            }
        courseBinding.coursePager.snackbar(messageRes = successMessage)
    }

    override fun showWishlistActionFailure(wishlistAction: WishlistAction) {
        @StringRes
        val errorMessage =
            if (wishlistAction == WishlistAction.ADD) {
                R.string.wishlist_action_add_failure
            } else {
                R.string.wishlist_action_remove_failure
            }
        courseBinding.coursePager.snackbar(messageRes = errorMessage)
    }

    override fun openCoursePurchaseInWeb(courseId: Long, queryParams: Map<String, List<String>>?) {
        val url = courseDeeplinkBuilder.createCourseLink(courseId, CourseScreenTab.PAY, queryParams)
        MagicLinkDialogFragment
            .newInstance(url, handleUrlInParent = true)
            .showIfNotExists(supportFragmentManager, MagicLinkDialogFragment.TAG)
    }

    override fun showTrialLesson(lessonId: Long, unitId: Long) {
        screenManager.showTrialLesson(this, lessonId, unitId)
    }

    override fun onDestroy() {
        releaseComponent(courseId)
        super.onDestroy()
    }

    override fun showCourse(course: Course, source: CourseViewSource, isAdaptive: Boolean) {
        if (isAdaptive) {
            screenManager.continueAdaptiveCourse(this, course)
        } else {
            courseBinding.coursePager.snackbar(messageRes = R.string.course_error_continue_learning)
        }
    }

    override fun showSteps(course: Course, source: CourseViewSource, lastStep: LastStep) {
        screenManager.continueCourse(this, lastStep)
    }

    override fun setBlockingLoading(isLoading: Boolean) {
        if (isLoading) {
            ProgressHelper.activate(progressDialogFragment, supportFragmentManager, LoadingProgressDialogFragment.TAG)
        } else {
            ProgressHelper.dismiss(supportFragmentManager, LoadingProgressDialogFragment.TAG)
        }
    }

    override fun onDismissed() {
        coursePresenter.handleCoursePurchasePressed()
    }

    override fun handleUrl(url: String) {
        InAppWebViewDialogFragment
            .newInstance(getString(R.string.course_purchase), url, isProvideAuth = false, isPaymentFlow = true)
            .showIfNotExists(supportFragmentManager, InAppWebViewDialogFragment.TAG)
    }

    override fun openCoursePurchaseInApp(coursePurchaseData: CoursePurchaseData) {
        showCoursePurchaseBottomSheetDialogFragment(coursePurchaseData, isNeedRestoreMessage = false)
    }

    override fun continueLearning() {
        coursePresenter.continueLearning()
    }

    private fun showCoursePurchaseBottomSheetDialogFragment(coursePurchaseData: CoursePurchaseData, isNeedRestoreMessage: Boolean) {
        CoursePurchaseBottomSheetDialogFragment
            .newInstance(coursePurchaseData, coursePurchaseSource = CoursePurchaseSource.COURSE_SCREEN_SOURCE, isNeedRestoreMessage = isNeedRestoreMessage)
            .showIfNotExists(supportFragmentManager, CoursePurchaseBottomSheetDialogFragment.TAG)
    }
}