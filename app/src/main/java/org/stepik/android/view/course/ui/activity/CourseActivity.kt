package org.stepik.android.view.course.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.android.synthetic.main.activity_course.*
import kotlinx.android.synthetic.main.error_course_not_found.*
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import kotlinx.android.synthetic.main.header_course.*
import kotlinx.android.synthetic.main.header_course_placeholder.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.experiments.CoursePurchasePriceSplitTest
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.ui.dialogs.LoadingProgressDialogFragment
import org.stepic.droid.ui.dialogs.UnauthorizedDialogFragment
import org.stepic.droid.ui.util.snackbar
import org.stepic.droid.util.ProgressHelper
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.Course
import org.stepik.android.presentation.course.CoursePresenter
import org.stepik.android.presentation.course.CourseView
import org.stepik.android.presentation.course.model.EnrollmentError
import org.stepik.android.view.course.routing.CourseScreenTab
import org.stepik.android.view.course.routing.getCourseIdFromDeepLink
import org.stepik.android.view.course.routing.getCourseTabFromDeepLink
import org.stepik.android.view.course.ui.adapter.CoursePagerAdapter
import org.stepik.android.view.course.ui.delegates.CourseHeaderDelegate
import org.stepik.android.view.course_content.ui.fragment.CourseContentFragment
import org.stepik.android.view.fragment_pager.FragmentDelegateScrollStateChangeListener
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import javax.inject.Inject

class CourseActivity : FragmentActivityBase(), CourseView {
    companion object {
        private const val EXTRA_COURSE = "course"
        private const val EXTRA_COURSE_ID = "course_id"
        private const val EXTRA_AUTO_ENROLL = "auto_enroll"
        private const val EXTRA_TAB = "tab"

        private const val NO_ID = -1L

        private const val UNAUTHORIZED_DIALOG_TAG = "unauthorized_dialog"

        fun createIntent(context: Context, course: Course, autoEnroll: Boolean = false, tab: CourseScreenTab = CourseScreenTab.INFO): Intent =
            Intent(context, CourseActivity::class.java)
                .putExtra(EXTRA_COURSE, course)
                .putExtra(EXTRA_AUTO_ENROLL, autoEnroll)
                .putExtra(EXTRA_TAB, tab.ordinal)

        fun createIntent(context: Context, courseId: Long, tab: CourseScreenTab = CourseScreenTab.INFO): Intent =
            Intent(context, CourseActivity::class.java)
                .putExtra(EXTRA_COURSE_ID, courseId)
                .putExtra(EXTRA_TAB, tab.ordinal)

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    private var courseId: Long = NO_ID
    private val analyticsOnPageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(page: Int) {
            if (coursePagerAdapter.getItem(page) is CourseContentFragment) {
                analytic
                    .reportAmplitudeEvent(
                        AmplitudeAnalytic.CourseReview.SCREEN_OPENED,
                        mapOf(AmplitudeAnalytic.CourseReview.Params.COURSE to courseId.toString())
                    )
            }
        }
    }
    private lateinit var coursePagerAdapter: CoursePagerAdapter
    private lateinit var coursePresenter: CoursePresenter
    private lateinit var courseHeaderDelegate: CourseHeaderDelegate

    private var unauthorizedDialogFragment: DialogFragment? = null
    private val progressDialogFragment: DialogFragment =
        LoadingProgressDialogFragment.newInstance()

    private val viewStateDelegate =
        ViewStateDelegate<CourseView.State>()

    private var viewPagerScrollState: Int =
        ViewPager.SCROLL_STATE_IDLE

    private var isInSwipeableViewState = false

    private var hasSavedInstanceState: Boolean = false

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

    @Inject
    internal lateinit var coursePurchasePriceSplitTest: CoursePurchasePriceSplitTest

//    @Inject
//    internal lateinit var billing: Billing

//    private lateinit var uiCheckout: UiCheckout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course)

        setSupportActionBar(courseToolbar)
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
        }

        if (course != null) {
            courseToolbarTitle.text = course.title
        }

        courseId = intent.getLongExtra(EXTRA_COURSE_ID, NO_ID)
            .takeIf { it != NO_ID }
            ?: course?.id
            ?: deepLinkCourseId
            ?: NO_ID

        injectComponent(courseId)
        coursePresenter = ViewModelProviders.of(this, viewModelFactory).get(CoursePresenter::class.java)
        courseHeaderDelegate =
            CourseHeaderDelegate(
                this, analytic, coursePresenter, coursePurchasePriceSplitTest,
                onSubmissionCountClicked = {
                    screenManager.showCachedAttempts(this, courseId)
                },
                isLocalSubmissionsEnabled = firebaseRemoteConfig.getBoolean(RemoteConfig.IS_LOCAL_SUBMISSIONS_ENABLED)
            )

//        uiCheckout = Checkout.forActivity(this, billing)
        initViewPager(courseId)
        initViewStateDelegate()

        hasSavedInstanceState = savedInstanceState != null

        setDataToPresenter()

        courseSwipeRefresh.setOnRefreshListener { setDataToPresenter(forceUpdate = true) }
        tryAgain.setOnClickListener { setDataToPresenter(forceUpdate = true) }
        goToCatalog.setOnClickListener {
            screenManager.showCatalog(this)
            finish()
        }
    }

    private fun setDataToPresenter(forceUpdate: Boolean = false) {
        val course: Course? = intent.getParcelableExtra(EXTRA_COURSE)
        if (course != null) {
            coursePresenter.onCourse(course, forceUpdate)
        } else {
            coursePresenter.onCourseId(courseId, forceUpdate)
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
        coursePager.addOnPageChangeListener(analyticsOnPageChangeListener)
        if (!hasSavedInstanceState) {
            setCurrentTab()
        }
    }

    override fun onPause() {
        coursePager.removeOnPageChangeListener(analyticsOnPageChangeListener)
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

        coursePager.currentItem =
            when (tab) {
                CourseScreenTab.REVIEWS -> 1
                CourseScreenTab.SYLLABUS -> 2
                else -> 0
            }
        if (coursePager.currentItem == 0) {
            analyticsOnPageChangeListener.onPageSelected(0)
        }
    }

    private fun initViewPager(courseId: Long) {
        val lightFont = ResourcesCompat.getFont(this, R.font.roboto_light)
        val regularFont = ResourcesCompat.getFont(this, R.font.roboto_regular)

        coursePagerAdapter = CoursePagerAdapter(courseId, this, supportFragmentManager)
        coursePager.adapter = coursePagerAdapter
        val onPageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageScrollStateChanged(scrollState: Int) {
                viewPagerScrollState = scrollState
                resolveSwipeRefreshState()
            }
        }
        coursePager.addOnPageChangeListener(FragmentDelegateScrollStateChangeListener(coursePager, coursePagerAdapter))
        coursePager.addOnPageChangeListener(onPageChangeListener)

        courseTabs.setupWithViewPager(coursePager)
        courseTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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

        for (i in 0 until courseTabs.tabCount) {
            val tab = courseTabs.getTabAt(i)
            tab?.customView = (layoutInflater.inflate(R.layout.view_course_tab, null) as TextView).also {
                it.typeface = lightFont
            }
        }

        (courseTabs.getTabAt(courseTabs.selectedTabPosition)?.customView as? TextView)
            ?.typeface = regularFont
    }

    private fun initViewStateDelegate() {
        viewStateDelegate.addState<CourseView.State.EmptyCourse>(courseEmpty)
        viewStateDelegate.addState<CourseView.State.NetworkError>(errorNoConnection)
        viewStateDelegate.addState<CourseView.State.CourseLoaded>(courseHeader, courseTabs, coursePager)
        viewStateDelegate.addState<CourseView.State.BlockingLoading>(courseHeader, courseTabs, coursePager)
        viewStateDelegate.addState<CourseView.State.Loading>(courseHeaderPlaceholder, courseTabs, coursePager)
        viewStateDelegate.addState<CourseView.State.Idle>(courseHeaderPlaceholder, courseTabs, coursePager)
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
        courseSwipeRefresh.isEnabled =
                viewPagerScrollState == ViewPager.SCROLL_STATE_IDLE &&
                    isInSwipeableViewState
    }

    override fun setState(state: CourseView.State) {
        courseSwipeRefresh.isRefreshing = false
        isInSwipeableViewState = (state is CourseView.State.CourseLoaded || state is CourseView.State.NetworkError)
        resolveSwipeRefreshState()

        when (state) {
            is CourseView.State.CourseLoaded -> {
                courseHeaderDelegate.courseHeaderData = state.courseHeaderData

                if (intent.getBooleanExtra(EXTRA_AUTO_ENROLL, false)) {
                    intent.removeExtra(EXTRA_AUTO_ENROLL)
                    coursePresenter.autoEnroll()

                    analytic.reportAmplitudeEvent(AmplitudeAnalytic.Course.JOINED, mapOf(
                        AmplitudeAnalytic.Course.Params.COURSE to state.courseHeaderData.courseId,
                        AmplitudeAnalytic.Course.Params.SOURCE to AmplitudeAnalytic.Course.Values.WIDGET
                    ))
                }

                ProgressHelper.dismiss(supportFragmentManager, LoadingProgressDialogFragment.TAG)
            }

            is CourseView.State.BlockingLoading -> {
                courseHeaderDelegate.courseHeaderData = state.courseHeaderData
                ProgressHelper.activate(progressDialogFragment, supportFragmentManager, LoadingProgressDialogFragment.TAG)
            }
        }
        viewStateDelegate.switchState(state)
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

                EnrollmentError.SERVER_ERROR ->
                    R.string.course_purchase_server_error

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

        coursePager.snackbar(messageRes = errorMessage)
    }

    override fun shareCourse(course: Course) {
        startActivity(shareHelper.getIntentForCourseSharing(course))
    }

    override fun showCourseShareTooltip() {
        courseHeaderDelegate.showCourseShareTooltip()
    }

    /**
     * BillingView
     */
//    override fun createUiCheckout(): UiCheckout =
//        uiCheckout

    override fun openCoursePurchaseInWeb(courseId: Long, queryParams: Map<String, List<String>>?) {
        screenManager.openCoursePurchaseInWeb(this, courseId, queryParams)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (!uiCheckout.onActivityResult(requestCode, resultCode, data)) {
//            super.onActivityResult(requestCode, resultCode, data)
//        }
//    }

    override fun onDestroy() {
        releaseComponent(courseId)
        super.onDestroy()
    }

    override fun showCourse(course: Course, isAdaptive: Boolean) {
        if (isAdaptive) {
            screenManager.continueAdaptiveCourse(this, course)
        } else {
            coursePager.snackbar(messageRes = R.string.course_error_continue_learning)
        }
    }

    override fun showSteps(course: Course, lastStep: LastStep) {
        screenManager.continueCourse(this, lastStep)
    }

    override fun setBlockingLoading(isLoading: Boolean) {
        if (isLoading) {
            ProgressHelper.activate(progressDialogFragment, supportFragmentManager, LoadingProgressDialogFragment.TAG)
        } else {
            ProgressHelper.dismiss(supportFragmentManager, LoadingProgressDialogFragment.TAG)
        }
    }
}