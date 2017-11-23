package org.stepic.droid.ui.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.novoda.spritz.Spritz
import com.novoda.spritz.SpritzStep
import kotlinx.android.synthetic.main.activity_onboarding.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentActivityBase
import java.util.concurrent.TimeUnit


class AnimatedOnboardingActivity : FragmentActivityBase() {

    private val PAGES_COUNT = 4

    var spritz: Spritz? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        lottieAnimationView.imageAssetsFolder = "images"
//        lottieAnimationView.enableMergePathsForKitKatAndAbove(true)
        onboardingViewPager.adapter = ScreenSlidePagerAdapter(supportFragmentManager)

        lottieAnimationView.enableMergePathsForKitKatAndAbove(true)
        lottieAnimationView.addAnimatorUpdateListener { value ->

        }

        spritz = Spritz.with(lottieAnimationView)
                .withSteps(
                        SpritzStep.Builder()
                                .withSwipeDuration(2002, TimeUnit.MILLISECONDS)
                                .build(),
                        SpritzStep.Builder()
                                .withSwipeDuration(2002, TimeUnit.MILLISECONDS)
                                .build(),
                        SpritzStep.Builder()
                                .withSwipeDuration(2002, TimeUnit.MILLISECONDS)
                                .build(),
                        SpritzStep.Builder()
                                .build()
                )
                .build()
    }

    override fun onStart() {
        super.onStart()
        spritz?.attachTo(onboardingViewPager)
        spritz?.startPendingAnimations()

    }

    override fun onStop() {
        super.onStop()
        spritz?.detachFrom(onboardingViewPager)
    }


    private inner class ScreenSlidePagerAdapter internal constructor(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {

        override fun getItem(position: Int): Fragment {
            return AnimationFragment()
        }

        override fun getCount(): Int {
            return PAGES_COUNT
        }

    }


    class AnimationFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.animation_page, container, false)

        }

    }
}
