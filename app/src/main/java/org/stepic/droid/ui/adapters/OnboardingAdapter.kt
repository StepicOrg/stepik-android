package org.stepic.droid.ui.adapters

import androidx.core.app.Fragment
import androidx.core.app.FragmentManager
import androidx.core.app.FragmentPagerAdapter
import org.stepic.droid.model.OnboardingType
import org.stepic.droid.ui.fragments.OnboardingFragment

class OnboardingAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    companion object {
        private const val TAB_COUNT = 4
    }

    override fun getItem(position: Int): Fragment {
        val type = OnboardingType.values()[position]
        return OnboardingFragment.newInstance(type)
    }

    override fun getCount() = TAB_COUNT
}
