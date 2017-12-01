package org.stepic.droid.ui.activities

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_onboarding.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentActivityBase


class AnimatedOnboardingActivity : FragmentActivityBase() {

    private val PAGES_COUNT = 4


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        lottieAnimationView.loop(true)
    }
}
