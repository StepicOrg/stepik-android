package org.stepic.droid.features.course.ui.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import org.stepic.droid.R

class CoursePagerAdapter(
        context: Context,
        fragmentManager: FragmentManager
) : FragmentPagerAdapter(fragmentManager) {
    private val fragments = listOf(
            ::Fragment to context.getString(R.string.course_tab_info),
            ::Fragment to context.getString(R.string.course_tab_reviews),
            ::Fragment to context.getString(R.string.course_tab_modules)
    )

    override fun getItem(position: Int): Fragment =
            fragments[position].first.invoke()

    override fun getCount(): Int =
            fragments.size

    override fun getPageTitle(position: Int): CharSequence =
            fragments[position].second
}