package org.stepic.droid.ui.activities

import android.os.Bundle
import org.stepic.droid.R
import org.stepic.droid.base.FragmentActivityBase


class AnimatedOnboardingActivity : FragmentActivityBase() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
    }
}
