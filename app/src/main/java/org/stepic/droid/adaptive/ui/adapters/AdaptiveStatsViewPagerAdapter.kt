package org.stepic.droid.adaptive.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.stepic.droid.adaptive.model.AdaptiveStatsTabs

class AdaptiveStatsViewPagerAdapter(
    activity: FragmentActivity,
    private val courseId: Long
) : FragmentStateAdapter(activity) {
    override fun createFragment(position: Int): Fragment =
        AdaptiveStatsTabs.values()[position].fragmentFactory.invoke(courseId)

    override fun getItemCount(): Int =
        AdaptiveStatsTabs.values().size
}