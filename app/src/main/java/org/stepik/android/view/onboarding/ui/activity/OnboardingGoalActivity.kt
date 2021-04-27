package org.stepik.android.view.onboarding.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_onboarding_goal.*
import kotlinx.android.synthetic.main.item_onboarding.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.view.onboarding.mapper.OnboardingRemoteConfigMapper
import org.stepik.android.view.onboarding.model.OnboardingGoal
import org.stepik.android.view.onboarding.ui.adapter.delegate.OnboardingGoalAdapterDelegate
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
    internal lateinit var onboardingRemoteConfigMapper: OnboardingRemoteConfigMapper

    private val onboardingGoalsAdapter: DefaultDelegateAdapter<OnboardingGoal> = DefaultDelegateAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component().inject(this)
        sharedPreferenceHelper.personalizedCourseList = -1L

        onboardingGoalsAdapter += OnboardingGoalAdapterDelegate { onboardingGoal ->
            // TODO APPS-3292: Analytic for choice
            startActivity(OnboardingCourseListsActivity.createIntent(this, onboardingGoal))
        }
        val items = onboardingRemoteConfigMapper.buildOnboardingGoals()
        onboardingGoalsAdapter.items = items

        goalRecycler.layoutManager = LinearLayoutManager(this)
        goalRecycler.adapter = onboardingGoalsAdapter

        val (icon, title) = getString(R.string.onboarding_goal_all_courses).split(' ', limit = 2)
        itemIcon.text = icon
        itemTitle.text = title

        dismissButton.setOnClickListener {
            closeOnboarding()
        }
        allCoursesAction.setOnClickListener {
            closeOnboarding()
        }
    }

    private fun closeOnboarding() {
        sharedPreferenceHelper.afterPersonalizedOnboardingEngaged()
        sharedPreferenceHelper.afterOnboardingPassed()
        screenManager.showLaunchScreen(this)
        finish()
    }
}