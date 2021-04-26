package org.stepik.android.view.onboarding.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_onboarding_goal.*
import kotlinx.android.synthetic.main.item_onboarding.*
import org.stepic.droid.R
import org.stepik.android.view.onboarding.model.OnboardingGoalItem
import org.stepik.android.view.onboarding.ui.adapter.delegate.OnboardingItemAdapterDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class OnboardingGoalActivity : AppCompatActivity(R.layout.activity_onboarding_goal) {
    companion object {
        fun createIntent(context: Context): Intent =
            Intent(context, OnboardingGoalActivity::class.java)

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    private val onboardingGoalsAdapter: DefaultDelegateAdapter<OnboardingGoalItem> = DefaultDelegateAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onboardingGoalsAdapter.items = listOf(
            OnboardingGoalItem(R.drawable.onboarding_goal_yellow_red_gradient, getString(R.string.onboarding_goal_career)),
            OnboardingGoalItem(R.drawable.onboarding_goal_blue_violet_gradient, getString(R.string.onboarding_goal_learn_new)),
            OnboardingGoalItem(R.drawable.onboarding_goal_yellow_green_gradient, getString(R.string.onboarding_goal_exams)),
            OnboardingGoalItem(R.drawable.onboarding_goal_blue_violet_gradient, getString(R.string.onboarding_goal_create_courses))
        )

        onboardingGoalsAdapter += OnboardingItemAdapterDelegate { onboardingGoalItem ->
            Toast.makeText(this, "Item chosen: ${onboardingGoalItem.itemTitle}", Toast.LENGTH_SHORT).show()
        }

        goalRecycler.layoutManager = LinearLayoutManager(this)
        goalRecycler.adapter = onboardingGoalsAdapter

        val (icon, title) = getString(R.string.onboarding_goal_all_courses).split(' ', limit = 2)
        itemIcon.text = icon
        itemTitle.text = title
        allCoursesAction.setOnClickListener { Toast.makeText(this, "All courses action", Toast.LENGTH_SHORT).show() }
        dismissButton.setOnClickListener { Toast.makeText(this, "Dismiss action", Toast.LENGTH_SHORT).show() }
    }
}