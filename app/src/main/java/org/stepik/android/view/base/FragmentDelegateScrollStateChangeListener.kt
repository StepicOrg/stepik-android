package org.stepik.android.view.base

import android.support.v4.view.ViewPager
import org.stepik.android.view.ui.listener.FragmentViewPagerScrollStateListener

class FragmentDelegateScrollStateChangeListener(
    private val viewPager: ViewPager,
    private val fragmentAdapter: ActiveFragmentPagerAdapter
) : ViewPager.SimpleOnPageChangeListener() {
    override fun onPageScrollStateChanged(state: Int) {
        fragmentAdapter
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