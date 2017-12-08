package org.stepic.droid.ui.activities

import android.os.Bundle
import android.support.v4.view.ViewPager
import kotlinx.android.synthetic.main.activity_onboarding.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.activities.contracts.OnNextClickedListener
import org.stepic.droid.ui.adapters.OnboardingAdapter
import org.stepic.droid.ui.custom.OnboardingPageTransformer
import org.stepic.droid.ui.fragments.OnboardingFragment
import timber.log.Timber


class AnimatedOnboardingActivity : FragmentActivityBase(), OnNextClickedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        initViewPager()
        initClose()
    }

    private fun initViewPager() {
        onboardingViewPager.adapter = OnboardingAdapter(supportFragmentManager)
        onboardingCircleIndicator.setViewPager(onboardingViewPager)
        val pageChangeListener: ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                invokeAnimationOnFragment(position)
            }
        }
        onboardingViewPager.addOnPageChangeListener(pageChangeListener)
        onboardingViewPager.setPageTransformer(false, OnboardingPageTransformer())

        //we should post animation on next frame
        onboardingViewPager.post { pageChangeListener.onPageSelected(onboardingViewPager.currentItem) }
    }

    private fun invokeAnimationOnFragment(position: Int) {
        val tag = "android:switcher:" + R.id.onboardingViewPager + ":" + position
        val fragment = supportFragmentManager.findFragmentByTag(tag)
        (fragment as OnboardingFragment).startAnimation()
    }

    private fun initClose() {
        closeOnboarding.setOnClickListener {
            onboardingClosed()
        }
    }

    private fun onboardingClosed() {
        val position = onboardingViewPager.currentItem
        Timber.d("closed at $position")
    }

    override fun onNextClicked() {
        val current = onboardingViewPager.currentItem
        val next = current + 1
        if (next < onboardingViewPager.adapter.count) {
            onboardingViewPager.setCurrentItem(next, true)
        } else {
            onboardingDone()
        }
    }

    private fun onboardingDone() {
        Timber.d("done")
    }

    override fun onBackPressed() {
        if (onboardingViewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            showPreviousSlide()
        }
    }

    private fun showPreviousSlide() {
        val previous = onboardingViewPager.currentItem - 1
        onboardingViewPager.setCurrentItem(previous, true)
    }

    override fun applyTransitionPrev() {
        // no-op
    }
}
