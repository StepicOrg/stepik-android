package org.stepic.droid.ui.activities

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_onboarding.*
import org.stepic.droid.R
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.analytic.experiments.DeferredAuthSplitTest
import org.stepic.droid.analytic.experiments.OnboardingSplitTestVersion2
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.activities.contracts.OnNextClickedListener
import org.stepic.droid.ui.adapters.OnboardingAdapter
import org.stepic.droid.ui.custom.OnboardingPageTransformer
import org.stepic.droid.ui.fragments.OnboardingFragment
import javax.inject.Inject

class AnimatedOnboardingActivity : FragmentActivityBase(), OnNextClickedListener {

    @Inject
    lateinit var deferredAuthSplitsTest: DeferredAuthSplitTest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_onboarding)
        App.component().inject(this)
        initViewPager()
        initClose()
    }

    private fun initViewPager() {
        val onboardingAdapter = OnboardingAdapter(supportFragmentManager)
        onboardingViewPager.adapter = onboardingAdapter
        onboardingViewPager.offscreenPageLimit = onboardingAdapter.count

        onboardingCircleIndicator.setViewPager(onboardingViewPager)

        val pageChangeListener: ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                invokeAnimationOnFragment(position)
                reportToAmplitude(AmplitudeAnalytic.Onboarding.SCREEN_OPENED)
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
        (fragment as? OnboardingFragment)?.startAnimation()
    }

    private fun initClose() {
        closeOnboarding.bringToFront()
        closeOnboarding.setOnClickListener {
            onboardingClosed()
        }
    }

    private fun onboardingClosed() {
        reportToAmplitude(AmplitudeAnalytic.Onboarding.CLOSED)
        openLaunchScreen()
    }

    override fun onNextClicked() {
        val current = onboardingViewPager.currentItem
        val next = current + 1
        if (next < onboardingViewPager.adapter!!.count) {
            onboardingViewPager.setCurrentItem(next, true)
        } else {
            onboardingComplete()
        }
    }


    private fun reportToAmplitude(eventName: String) {
        val analyticPosition = onboardingViewPager.currentItem + 1
        analytic.reportAmplitudeEvent(eventName, mapOf(AmplitudeAnalytic.Onboarding.PARAM_SCREEN to analyticPosition))
    }

    private fun onboardingComplete() {
        analytic.reportAmplitudeEvent(AmplitudeAnalytic.Onboarding.COMPLETED)
        openLaunchScreen()
    }

    private fun openLaunchScreen() {
        sharedPreferenceHelper.afterOnboardingPassed()
//        if (deferredAuthSplitsTest.currentGroup.isDeferredAuth && !deferredAuthSplitsTest.currentGroup.isCanDismissLaunch) {
//            screenManager.showMainFeed(this, MainFeedActivity.CATALOG_INDEX)
//        } else {
//            screenManager.showLaunchFromSplash(this)
//        }
        screenManager.showLaunchScreen(this)
        finish()
    }

    override fun onBackPressed() {
        if (isFirstItem()) {
            super.onBackPressed()
        } else {
            showPreviousSlide()
        }
    }

    private fun isFirstItem() = onboardingViewPager.currentItem == 0

    private fun showPreviousSlide() {
        val previous = onboardingViewPager.currentItem - 1
        onboardingViewPager.setCurrentItem(previous, true)
    }

    override fun applyTransitionPrev() {
        // no-op
    }
}
