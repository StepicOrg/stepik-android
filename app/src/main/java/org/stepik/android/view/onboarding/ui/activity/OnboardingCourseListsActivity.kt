package org.stepik.android.view.onboarding.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_onboarding_course_lists.*
import kotlinx.android.synthetic.main.item_onboarding.view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.ui.activities.MainFeedActivity
import org.stepik.android.domain.onboarding.analytic.OnboardingOpenedAnalyticEvent
import org.stepik.android.domain.onboarding.analytic.OnboardingCourseListSelectedAnalyticEvent
import org.stepik.android.domain.onboarding.analytic.OnboardingCompletedAnalyticEvent
import org.stepik.android.domain.onboarding.analytic.OnboardingClosedAnalyticEvent
import org.stepik.android.domain.onboarding.analytic.OnboardingBackToGoalsAnalyticEvent
import org.stepik.android.view.onboarding.model.OnboardingCourseList
import org.stepik.android.view.onboarding.model.OnboardingGoal
import ru.nobird.android.ui.adapterdelegates.dsl.adapterDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import javax.inject.Inject

class OnboardingCourseListsActivity : AppCompatActivity(R.layout.activity_onboarding_course_lists) {
    companion object {
        private const val EXTRA_ONBOARDING_GOAL = "onboarding_goal"
        fun createIntent(context: Context, onboardingGoal: OnboardingGoal): Intent =
            Intent(context, OnboardingCourseListsActivity::class.java)
                .putExtra(EXTRA_ONBOARDING_GOAL, onboardingGoal)

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    private val onboardingGoal by lazy { requireNotNull(intent.getParcelableExtra<OnboardingGoal>(EXTRA_ONBOARDING_GOAL)) }

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    private val courseListsAdapter: DefaultDelegateAdapter<OnboardingCourseList> = DefaultDelegateAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component().inject(this)
        analytic.report(OnboardingOpenedAnalyticEvent(screen = 2))

        courseListsHeader.text = onboardingGoal.title

        courseListsAdapter += createCourseListsAdapterDelegate { onboardingCourseList ->
            sharedPreferenceHelper.personalizedCourseList = onboardingCourseList.id
            analytic.report(OnboardingCourseListSelectedAnalyticEvent(onboardingCourseList.title, onboardingCourseList.id))
            analytic.report(OnboardingCompletedAnalyticEvent)
            closeOnboarding()
        }
        courseListsAdapter.items = onboardingGoal.courseLists
        courseListsRecycler.layoutManager = LinearLayoutManager(this)
        courseListsRecycler.adapter = courseListsAdapter

        backAction.setOnClickListener {
            onBackPressed()
        }

        dismissButton.setOnClickListener {
            analytic.report(OnboardingClosedAnalyticEvent(screen = 2))
            closeOnboarding()
        }
    }

    override fun onBackPressed() {
        analytic.report(OnboardingBackToGoalsAnalyticEvent)
        super.onBackPressed()
    }

    private fun createCourseListsAdapterDelegate(onItemClicked: (OnboardingCourseList) -> Unit) =
        adapterDelegate<OnboardingCourseList, OnboardingCourseList>(layoutResId = R.layout.item_onboarding) {
            val itemContainer = itemView.itemContainer
            val itemIcon = itemView.itemIcon
            val itemTitle = itemView.itemTitle

            itemView.setOnClickListener { item?.let(onItemClicked) }

            onBind { data ->
                itemIcon.text = data.icon
                itemTitle.text = data.title
                itemIcon.setBackgroundResource(R.drawable.onboarding_goal_yellow_green_gradient)
                if (data.isFeatured) {
                    itemContainer.setBackgroundResource(R.drawable.onboarding_featured_background)
                }
            }
        }

    private fun closeOnboarding() {
        sharedPreferenceHelper.afterOnboardingPassed()
        sharedPreferenceHelper.setPersonalizedOnboardingWasShown()
        val isLogged = sharedPreferenceHelper.authResponseFromStore != null
        if (isLogged) {
            screenManager.showMainFeed(this, MainFeedActivity.CATALOG_INDEX)
        } else {
            screenManager.showLaunchScreen(this)
        }
        finish()
    }
}