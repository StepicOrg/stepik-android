package org.stepik.android.view.onboarding.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_onboarding_goal.*
import kotlinx.android.synthetic.main.item_onboarding.*
import kotlinx.android.synthetic.main.item_onboarding.view.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.ui.activities.MainFeedActivity
import org.stepik.android.domain.onboarding.analytic.OnboardingAllCoursesAnalyticEvent
import org.stepik.android.domain.onboarding.analytic.OnboardingClosedAnalyticEvent
import org.stepik.android.domain.onboarding.analytic.OnboardingGoalSelectedAnalyticEvent
import org.stepik.android.domain.onboarding.analytic.OnboardingOpenedAnalyticEvent
import org.stepik.android.view.onboarding.resolver.OnboardingRemoteConfigResolver
import org.stepik.android.view.onboarding.model.IconBackground
import org.stepik.android.view.onboarding.model.OnboardingGoal
import ru.nobird.android.ui.adapterdelegates.dsl.adapterDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import javax.inject.Inject

class OnboardingGoalActivity : AppCompatActivity(R.layout.activity_onboarding_goal) {
    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, OnboardingGoalActivity::class.java)

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var screenManager: ScreenManager

    @Inject
    internal lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    @Inject
    internal lateinit var onboardingRemoteConfigResolver: OnboardingRemoteConfigResolver

    private val onboardingGoalsAdapter: DefaultDelegateAdapter<OnboardingGoal> = DefaultDelegateAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component().inject(this)
        sharedPreferenceHelper.personalizedCourseList = -1L
        analytic.report(OnboardingOpenedAnalyticEvent(screen = 1))

        onboardingGoalsAdapter += createGoalsAdapterDelegate { onboardingGoal ->
            analytic.report(OnboardingGoalSelectedAnalyticEvent(goalTitle = onboardingGoal.title))
            startActivity(OnboardingCourseListsActivity.createIntent(this, onboardingGoal))
        }
        val items = onboardingRemoteConfigResolver.buildOnboardingGoals()
        onboardingGoalsAdapter.items = items

        goalRecycler.layoutManager = LinearLayoutManager(this)
        goalRecycler.adapter = onboardingGoalsAdapter

        val (icon, title) = getString(R.string.onboarding_goal_all_courses).split(' ', limit = 2)
        itemIcon.text = icon
        itemTitle.text = title

        dismissButton.setOnClickListener {
            analytic.report(OnboardingClosedAnalyticEvent(screen = 1))
            onBackPressed()
        }
        allCoursesAction.setOnClickListener {
            analytic.report(OnboardingAllCoursesAnalyticEvent)
            onBackPressed()
        }
    }

    override fun onBackPressed() {
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

    private fun createGoalsAdapterDelegate(onItemClicked: (OnboardingGoal) -> Unit) =
        adapterDelegate<OnboardingGoal, OnboardingGoal>(layoutResId = R.layout.item_onboarding) {
            val itemIcon = itemView.itemIcon
            val itemTitle = itemView.itemTitle

            itemView.setOnClickListener { item?.let(onItemClicked) }

            onBind { data ->
                itemIcon.text = data.icon
                itemTitle.text = data.title
                val backgroundRes = IconBackground.values()[adapterPosition % IconBackground.values().size].backgroundRes
                itemIcon.setBackgroundResource(backgroundRes)
            }
        }
}