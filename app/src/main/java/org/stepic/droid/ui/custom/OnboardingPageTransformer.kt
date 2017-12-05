package org.stepic.droid.ui.custom

import android.support.v4.view.ViewPager
import android.view.View
import com.airbnb.lottie.LottieAnimationView
import org.stepic.droid.R
import timber.log.Timber

class OnboardingPageTransformer : ViewPager.PageTransformer {

    override fun transformPage(rootView: View, position: Float) {
        val viewPager = rootView.parent as ViewPager
        val page = rootView.findViewById<LottieAnimationView>(R.id.onboardingAnimationView)
        val percentage = 1 - Math.abs(position)
        page.cameraDistance = 12000f
        setVisibility(page, position)
        setTranslation(rootView, page, viewPager)
//        setSize(page, position, percentage)
        setRotation(page, position, percentage)
    }

    private fun setVisibility(page: View, position: Float) {
        if (position < 0.5 && position > -0.5) {
            page.visibility = View.VISIBLE
        } else {
            page.visibility = View.INVISIBLE
        }
    }

    private fun setTranslation(rootView: View, page: View, viewPager: ViewPager) {
        val scrollX = viewPager.scrollX
        val left = rootView.left
        val scroll = scrollX - left
        Timber.d("$scroll = $scrollX - $left")
        page.translationX = scroll.toFloat()
    }

    private fun setSize(page: View, position: Float, percentage: Float) {
        page.scaleX = if (position != 0f && position != 1f) percentage else 1f
        page.scaleY = if (position != 0f && position != 1f) percentage else 1f
    }

    private fun setRotation(page: View, position: Float, percentage: Float) {
        if (position > 0) {
            page.rotationY = -180 * (percentage + 1)
        } else {
            page.rotationY = 180 * (percentage + 1)
        }
    }
}
