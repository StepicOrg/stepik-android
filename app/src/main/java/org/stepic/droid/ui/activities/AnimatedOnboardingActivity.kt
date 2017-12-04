package org.stepic.droid.ui.activities

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_onboarding.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.adapters.OnboardingAdapter


class AnimatedOnboardingActivity : FragmentActivityBase() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        initViewPager()
    }

    private fun initViewPager() {
        onboardingViewPager.adapter = OnboardingAdapter(supportFragmentManager)
        onboardingCircleIndicator.setViewPager(onboardingViewPager)
    }
}
