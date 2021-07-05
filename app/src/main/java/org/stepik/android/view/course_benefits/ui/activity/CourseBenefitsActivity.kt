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
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepik.android.presentation.course_benefits.CourseBenefitsFeature
import org.stepik.android.presentation.course_benefits.CourseBenefitsViewModel
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

        private const val NO_ID = -1L

        fun createIntent(context: Context, courseId: Long): Intent =
            Intent(context, CourseBenefitsActivity::class.java)
                .putExtra(EXTRA_COURSE_ID, courseId)
    }

    private var courseId: Long = NO_ID

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private val courseBenefitsViewModel: CourseBenefitsViewModel by reduxViewModel(this) { viewModelFactory }

    private lateinit var courseBenefitSummaryDelegate: CourseBenefitSummaryViewDelegate

    private val viewStateDelegate = ViewStateDelegate<CourseBenefitsFeature.CourseBenefitState>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_benefits)

        setSupportActionBar(courseBenefitToolbar)
        val actionBar = this.supportActionBar
            ?: throw IllegalStateException("support action bar should be set")

        with(actionBar) {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }

        courseBenefitsAppBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            val ratio = abs(verticalOffset).toFloat() / (courseBenefitsCollapsingToolbar.height - courseBenefitToolbar.height)
            courseBenefitsToolbarScrim.alpha = ratio * 1.5f
        })

        injectComponent()
        initViewPager()
        initViewStateDelegate()
        courseId = intent.getLongExtra(EXTRA_COURSE_ID, NO_ID)
        courseBenefitSummaryDelegate = CourseBenefitSummaryViewDelegate(courseBenefitSummaryContainer)
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