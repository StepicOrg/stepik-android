package org.stepik.android.view.ui.listener

interface FragmentViewPagerScrollStateListener {
    fun onViewPagerScrollStateChanged(scrollState: ScrollState)

    enum class ScrollState {
        SCROLLING,
        INACTIVE, // current fragment inactive
        ACTIVE // current fragment active
    }
}