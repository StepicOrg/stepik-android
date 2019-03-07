package org.stepik.android.view.course.listener

import android.support.v4.view.ViewPager
import org.stepik.android.view.course.ui.adapter.CoursePagerAdapter
import org.stepik.android.view.ui.listener.FragmentViewPagerScrollStateListener

class CourseFragmentPageChangeListener(
    private val viewPager: ViewPager,
    private val coursePagerAdapter: CoursePagerAdapter
) : ViewPager.SimpleOnPageChangeListener() {
    override fun onPageScrollStateChanged(state: Int) {
        coursePagerAdapter
            .activeFragments
            .entries
            .forEach { (position, fragment) ->
                if (fragment is FragmentViewPagerScrollStateListener) {
                    val scrollState =
                        if (state == ViewPager.SCROLL_STATE_IDLE) {
                            if (position == viewPager.currentItem) {
                                FragmentViewPagerScrollStateListener.ScrollState.ACTIVE
                            } else {
                                FragmentViewPagerScrollStateListener.ScrollState.INACTIVE
                            }
                        } else {
                            FragmentViewPagerScrollStateListener.ScrollState.SCROLLING
                        }

                    fragment.onViewPagerScrollStateChanged(scrollState)
                }
            }
    }
}