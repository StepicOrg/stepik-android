package org.stepic.droid.adaptive.ui.adapters

import android.content.Context
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import org.stepic.droid.R
import org.stepic.droid.adaptive.ui.fragments.AdaptiveRatingFragment

class AdaptiveStatsViewPagerAdapter(fm: FragmentManager, context: Context, courseId: Long) : FragmentStatePagerAdapter(fm) {
    private val fragments = listOf(
            { AdaptiveRatingFragment.newInstance(courseId) } to context.getString(R.string.adaptive_rating)
    )

    override fun getItem(position: Int) = fragments[position].first()
    override fun getCount(): Int = fragments.size
    override fun getPageTitle(position: Int): String = fragments[position].second
}