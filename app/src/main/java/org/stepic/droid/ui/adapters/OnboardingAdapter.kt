package org.stepic.droid.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import org.stepic.droid.model.OnboardingType
import org.stepic.droid.ui.fragments.OnboardingFragment

class OnboardingAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    companion object {
        private const val TAB_COUNT = 4
    }

    override fun getItem(position: Int): Fragment {
        val type = OnboardingType.values()[position]
        return OnboardingFragment.newInstance(type)
    }

    override fun getCount(): Int =
        TAB_COUNT
}
