package org.stepic.droid.ui.listeners

import android.support.v4.view.ViewPager
import org.stepic.droid.ui.adapters.StepFragmentAdapter
import org.stepik.android.view.ui.listener.FragmentViewPagerScrollStateListener

class StepFragmentPageChangeListener(
    private val viewPager: ViewPager,
    private val stepFragmentAdapter: StepFragmentAdapter
) : ViewPager.SimpleOnPageChangeListener() {
    override fun onPageScrollStateChanged(state: Int) {
        stepFragmentAdapter
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