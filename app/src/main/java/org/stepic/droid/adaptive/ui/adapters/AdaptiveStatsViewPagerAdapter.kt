package org.stepic.droid.adaptive.ui.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import org.stepic.droid.R
import org.stepic.droid.adaptive.ui.fragments.AdaptiveProgressFragment
import org.stepic.droid.adaptive.ui.fragments.AdaptiveRatingFragment

class AdaptiveStatsViewPagerAdapter(
    fm: FragmentManager,
    context: Context,
    courseId: Long
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val fragments = listOf(
            { AdaptiveProgressFragment.newInstance(courseId) } to context.getString(R.string.adaptive_progress),
            { AdaptiveRatingFragment.newInstance(courseId) }   to context.getString(R.string.adaptive_rating)
    )

    override fun getItem(position: Int): Fragment =
        fragments[position].first()

    override fun getCount(): Int =
        fragments.size

    override fun getPageTitle(position: Int): String =
        fragments[position].second
}