package org.stepik.android.view.course_benefits.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.activity_course_benefits.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepik.android.domain.course_benefits.analytic.CourseBenefitsScreenOpenedEvent
import org.stepik.android.domain.course_benefits.analytic.CourseBenefitsSummaryClicked
import org.stepik.android.presentation.course_benefits.CourseBenefitsFeature
import org.stepik.android.presentation.course_benefits.CourseBenefitsViewModel
import org.stepik.android.view.course.mapper.DisplayPriceMapper
import org.stepik.android.view.course_benefits.ui.adapter.CourseBenefitPagerAdapter
import org.stepik.android.view.course_benefits.ui.delegate.CourseBenefitSummaryViewDelegate
import ru.nobird.android.presentation.redux.container.ReduxView
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import javax.inject.Inject
import kotlin.math.abs

class CourseBenefitsActivity : AppCompatActivity(), ReduxView<CourseBenefitsFeature.State, CourseBenefitsFeature.Action.ViewAction> {
    companion object {
        private const val EXTRA_COURSE_ID = "course_id"
        private const val EXTRA_COURSE_TITLE = "course_title"

        private const val NO_ID = -1L

        fun createIntent(context: Context, courseId: Long, courseTitle: String?): Intent =
            Intent(context, CourseBenefitsActivity::class.java)
                .putExtra(EXTRA_COURSE_ID, courseId)
                .putExtra(EXTRA_COURSE_TITLE, courseTitle)
    }

    private var courseId: Long = NO_ID
    private var courseTitle: String? = null

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    internal lateinit var displayPriceMapper: DisplayPriceMapper

    private val courseBenefitsViewModel: CourseBenefitsViewModel by reduxViewModel(this) { viewModelFactory }

    private lateinit var courseBenefitSummaryDelegate: CourseBenefitSummaryViewDelegate

    private val viewStateDelegate = ViewStateDelegate<CourseBenefitsFeature.CourseBenefitState>()

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

        courseBenefitToolbarTitle.text = getString(R.string.course_benefits_toolbar_title, courseTitle)

        courseBenefitsAppBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            val ratio = abs(verticalOffset).toFloat() / (courseBenefitsCollapsingToolbar.height - courseBenefitToolbar.height)
            courseBenefitsToolbarScrim.alpha = ratio * 1.5f
        })

        initViewPager()
        initViewStateDelegate()
        courseBenefitSummaryDelegate = CourseBenefitSummaryViewDelegate(courseBenefitSummaryContainer, displayPriceMapper) { isExpanded -> analytic.report(CourseBenefitsSummaryClicked(courseId, courseTitle, isExpanded)) }
        courseBenefitsViewModel.onNewMessage(CourseBenefitsFeature.Message.InitMessage(courseId, forceUpdate = false))
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
        viewStateDelegate.addState<CourseBenefitsFeature.CourseBenefitState.Idle>()
        viewStateDelegate.addState<CourseBenefitsFeature.CourseBenefitState.Loading>(courseBenefitsTabs, courseBenefitSummaryContainer, courseBenefitsOperationsViewPager)
        viewStateDelegate.addState<CourseBenefitsFeature.CourseBenefitState.Error>(coursesBenefitsLoadingError)
        viewStateDelegate.addState<CourseBenefitsFeature.CourseBenefitState.Content>(courseBenefitsTabs, courseBenefitSummaryContainer, courseBenefitsOperationsViewPager)
    }

    private fun initViewPager() {
        val pagerAdapter = CourseBenefitPagerAdapter(this)

        courseBenefitsOperationsViewPager.adapter = pagerAdapter
        courseBenefitsTabs.setupWithViewPager(courseBenefitsOperationsViewPager)
    }

    override fun onAction(action: CourseBenefitsFeature.Action.ViewAction) {
        // no op
    }

    override fun render(state: CourseBenefitsFeature.State) {
        viewStateDelegate.switchState(state.courseBenefitState)
        courseBenefitSummaryDelegate.render(state.courseBenefitSummaryState)
    }
}