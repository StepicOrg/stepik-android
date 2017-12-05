package org.stepic.droid.ui.activities

import android.animation.Animator
import android.animation.AnimatorInflater
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import kotlinx.android.synthetic.main.activity_onboarding.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentActivityBase
import org.stepic.droid.ui.activities.contracts.OnNextClickedListener
import org.stepic.droid.ui.adapters.OnboardingAdapter
import org.stepic.droid.ui.custom.OnboardingPageTransformer
import timber.log.Timber


class AnimatedOnboardingActivity : FragmentActivityBase(), OnNextClickedListener {
    val setRightOut: Animator = AnimatorInflater.loadAnimator(App.getAppContext(), R.animator.onboarding_animation_out)
    val setLeftIn: Animator = AnimatorInflater.loadAnimator(App.getAppContext(), R.animator.onboarding_animation_in)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        initViewPager()
        initClose()
        val position = 0 //todo: get from view pager
        setAnimation(position)
    }


    private fun initViewPager() {
        onboardingViewPager.adapter = OnboardingAdapter(supportFragmentManager)
        onboardingCircleIndicator.setViewPager(onboardingViewPager)
        onboardingViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                Timber.d("onPageSelected $position")
                setAnimation(position)
            }
        })
        onboardingViewPager.setPageTransformer(false, OnboardingPageTransformer())
    }

    private fun initClose() {
        closeOnboarding.setOnClickListener {
            onboardingClosed()
        }
    }

    private fun setAnimation(position: Int) {
//        onboardingAnimationView.pauseAnimation()
//        onboardingAnimationView.speed = 0.4f
//        onboardingAnimationView.loop(true)
//
////        val distance = 8000
////        val scale = resources.displayMetrics.density
////        onboardingAnimationView.cameraDistance = distance * scale
////        onboardingAnimationView2.cameraDistance = distance * scale
//
//        val animationFile = when (position) {
//            0 -> {
//                FIRST_ANIMATION_PATH
//            }
//            1 -> {
//                SECOND_ANIMATION_PATH
//            }
//            2 -> THIRD_ANIMATION_PATH
//            3 -> FOURTH_ANIMATION_PATH
//            else -> throw IllegalStateException("Add animation file for onboarding")
//        }
//        onboardingAnimationView.setAnimation(animationFile)
//        onboardingAnimationView.playAnimation()
    }

    private fun onboardingClosed() {
        val position = onboardingViewPager.currentItem
        Timber.d("closed at $position")
    }

    override fun onNextClicked() {
        val current = onboardingViewPager.currentItem
        val next = current + 1
        if (next < onboardingViewPager.adapter.count) {
            onboardingViewPager.setCurrentItem(next)
        } else {
            onboardingDone()
        }
    }

    private fun onboardingDone() {
        Timber.d("done")
    }

    override fun applyTransitionPrev() {
        // no-op
    }

    fun flipCard(outView: View, inView: View) {
        setRightOut.setTarget(outView)
        setLeftIn.setTarget(inView)
        setRightOut.start()
        setLeftIn.start()
    }

}
