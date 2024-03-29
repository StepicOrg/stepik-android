package org.stepik.android.view.course_revenue.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_course_benefits.*
import kotlinx.android.synthetic.main.error_no_connection_with_button.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.util.DeviceInfoUtil
import org.stepik.android.domain.course_revenue.analytic.CourseBenefitClickedEvent
import org.stepik.android.domain.course_revenue.analytic.CourseBenefitsScreenOpenedEvent
import org.stepik.android.domain.course_revenue.analytic.CourseBenefitsSummaryClicked
import org.stepik.android.domain.course_revenue.model.CourseBeneficiary
import org.stepik.android.presentation.course_revenue.CourseBenefitsFeature
import org.stepik.android.presentation.course_revenue.CourseBenefitsMonthlyFeature
import org.stepik.android.presentation.course_revenue.CourseRevenueFeature
import org.stepik.android.presentation.course_revenue.CourseRevenueViewModel
import org.stepik.android.view.course_revenue.mapper.RevenuePriceMapper
import org.stepik.android.view.course_revenue.model.CourseBenefitOperationItem
import org.stepik.android.view.course_revenue.ui.adapter.delegate.CourseBenefitsListAdapterDelegate
import org.stepik.android.view.course_revenue.ui.delegate.CourseBenefitSummaryViewDelegate
import org.stepik.android.view.course_revenue.ui.adapter.delegate.CourseBenefitsMonthlyListAdapterDelegate
import org.stepik.android.view.course_revenue.ui.dialog.TransactionBottomSheetDialogFragment
import ru.nobird.app.presentation.redux.container.ReduxView
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import javax.inject.Inject
import kotlin.math.abs

class CourseRevenueActivity : AppCompatActivity(), ReduxView<CourseRevenueFeature.State, CourseRevenueFeature.Action.ViewAction> {
    companion object {
        private const val EXTRA_COURSE_ID = "course_id"
        private const val EXTRA_COURSE_TITLE = "course_title"

        private const val NO_ID = -1L

        fun createIntent(context: Context, courseId: Long, courseTitle: String?): Intent =
            Intent(context, CourseRevenueActivity::class.java)
                .putExtra(EXTRA_COURSE_ID, courseId)
                .putExtra(EXTRA_COURSE_TITLE, courseTitle)
    }

    private var courseId: Long = NO_ID
    private var courseTitle: String? = null
    private var courseBeneficiary: CourseBeneficiary? = null

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var revenuePriceMapper: RevenuePriceMapper

    private val courseRevenueViewModel: CourseRevenueViewModel by reduxViewModel(this) { viewModelFactory }

    private lateinit var courseBenefitSummaryDelegate: CourseBenefitSummaryViewDelegate

    private val viewStateDelegate = ViewStateDelegate<CourseRevenueFeature.CourseRevenueState>()

    private val courseBenefitsOperationsItemAdapter: DefaultDelegateAdapter<CourseBenefitOperationItem> = DefaultDelegateAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_benefits)
        injectComponent()

        setSupportActionBar(courseBenefitToolbar)
        val actionBar = this.supportActionBar
            ?: throw IllegalStateException("support action bar should be set")

        with(actionBar) {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }
        courseId = intent.getLongExtra(EXTRA_COURSE_ID, NO_ID)
        courseTitle = intent.getStringExtra(EXTRA_COURSE_TITLE)

        analytic.report(CourseBenefitsScreenOpenedEvent(courseId, courseTitle))

        courseBenefitToolbarTitle.text = if (courseTitle.isNullOrBlank()) {
            getString(R.string.course_benefits_revenue_title)
        } else {
            getString(R.string.course_benefits_toolbar_title, courseTitle)
        }

        courseBenefitsAppBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            val ratio = abs(verticalOffset).toFloat() / (courseBenefitsCollapsingToolbar.height - courseBenefitToolbar.height)
            courseBenefitSummaryContainer.alpha = 1f - (ratio * 1.5f)
        })

        ViewCompat.setTranslationZ(divider, ViewCompat.getElevation(courseBenefitsAppBar))

        initViewPager()
        initViewStateDelegate()
        courseBenefitSummaryDelegate = CourseBenefitSummaryViewDelegate(courseBenefitSummaryContainer,
            revenuePriceMapper,
            onCourseSummaryClicked = { isExpanded -> analytic.report(CourseBenefitsSummaryClicked(courseId, courseTitle, isExpanded)) },
            onContactSupportClicked = {
                courseRevenueViewModel.onNewMessage(
                    CourseRevenueFeature.Message.SetupFeedback(
                        getString(R.string.feedback_subject),
                        DeviceInfoUtil.getInfosAboutDevice(this, "\n"
                        )
                    )
                )
            }
        )
        courseRevenueViewModel.onNewMessage(CourseRevenueFeature.Message.InitMessage(courseId, forceUpdate = false))

        tryAgain.setOnClickListener { courseRevenueViewModel.onNewMessage(CourseRevenueFeature.Message.InitMessage(courseId, forceUpdate = true)) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    private fun injectComponent() {
        App.component()
            .courseBenefitsComponentBuilder()
            .build()
            .inject(this)
    }

    private fun initViewStateDelegate() {
        viewStateDelegate.addState<CourseRevenueFeature.CourseRevenueState.Idle>()
        viewStateDelegate.addState<CourseRevenueFeature.CourseRevenueState.Loading>(courseBenefitsTabs, courseBenefitSummaryContainer, courseBenefitsOperationsViewPager)
        viewStateDelegate.addState<CourseRevenueFeature.CourseRevenueState.Error>(coursesBenefitsLoadingError)
        viewStateDelegate.addState<CourseRevenueFeature.CourseRevenueState.Content>(courseBenefitsTabs, courseBenefitSummaryContainer, courseBenefitsOperationsViewPager)
    }

    private fun initViewPager() {
        courseBenefitsOperationsItemAdapter += CourseBenefitsListAdapterDelegate(
            revenuePriceMapper,
            onItemClick = {
                val courseBeneficiary = courseBeneficiary ?: return@CourseBenefitsListAdapterDelegate
                analytic.report(
                    CourseBenefitClickedEvent(
                        it.courseBenefit.id,
                        it.courseBenefit.status,
                        courseId,
                        courseTitle
                    )
                )
                TransactionBottomSheetDialogFragment
                    .newInstance(it.courseBenefit, courseBeneficiary, it.user, courseTitle)
                    .showIfNotExists(supportFragmentManager, TransactionBottomSheetDialogFragment.TAG)
            },
            onFetchNextPage = { courseRevenueViewModel.onNewMessage(CourseRevenueFeature.Message.CourseBenefitsMessage(CourseBenefitsFeature.Message.FetchNextPage(courseId))) },
            reloadListAction = { courseRevenueViewModel.onNewMessage(CourseRevenueFeature.Message.CourseBenefitsMessage(CourseBenefitsFeature.Message.TryAgain(courseId))) }
        )

        courseBenefitsOperationsItemAdapter += CourseBenefitsMonthlyListAdapterDelegate(
            revenuePriceMapper,
            onFetchNextPage = { courseRevenueViewModel.onNewMessage(CourseRevenueFeature.Message.CourseBenefitsMonthlyMessage(CourseBenefitsMonthlyFeature.Message.FetchCourseBenefitsByMonthNext(courseId))) },
            reloadListAction = { courseRevenueViewModel.onNewMessage(CourseRevenueFeature.Message.CourseBenefitsMonthlyMessage(CourseBenefitsMonthlyFeature.Message.TryAgain(courseId))) }
        )

        courseBenefitsOperationsViewPager.adapter = courseBenefitsOperationsItemAdapter
        courseBenefitsTabs.addTab(courseBenefitsTabs.newTab().setText(getString(R.string.course_benefits_tab)))
        courseBenefitsTabs.addTab(courseBenefitsTabs.newTab().setText(R.string.course_benefits_monthly_tab))
        courseBenefitsTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: return
                courseBenefitsOperationsViewPager.currentItem = position
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // no op
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // no op
            }
        })
        courseBenefitsOperationsViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                courseBenefitsTabs.selectTab(courseBenefitsTabs.getTabAt(position))
            }
        })
    }

    override fun onAction(action: CourseRevenueFeature.Action.ViewAction) {
        if (action is CourseRevenueFeature.Action.ViewAction.ShowContactSupport) {
            screenManager.openTextFeedBack(this, action.supportEmailData)
        }
    }

    override fun render(state: CourseRevenueFeature.State) {
        viewStateDelegate.switchState(state.courseRevenueState)
        courseBenefitSummaryDelegate.render(state.courseBenefitSummaryState)
        courseBenefitsOperationsItemAdapter.items = listOf(
            CourseBenefitOperationItem.CourseBenefits(state.courseBenefitsState),
            CourseBenefitOperationItem.CourseBenefitsMonthly(state.courseBenefitsMonthlyState)
        )
        if (state.courseBenefitsState is CourseBenefitsFeature.State.Content) {
            courseBeneficiary = state.courseBenefitsState.courseBeneficiary
        }
    }
}