package org.stepic.droid.ui.custom

import android.view.View
import androidx.viewpager.widget.ViewPager
import com.airbnb.lottie.LottieAnimationView
import org.stepic.droid.R

class OnboardingPageTransformer : ViewPager.PageTransformer {

    private val distance = 20000f

    override fun transformPage(rootView: View, position: Float) {
        val viewPager = rootView.parent as ViewPager
        val page = rootView.findViewById<LottieAnimationView>(R.id.onboardingAnimationView)
        val percentage = 1 - Math.abs(position)
        val scale = rootView.resources.displayMetrics.density
        page.cameraDistance = scale * distance
        setVisibility(page, position)
        setTranslation(rootView, page, viewPager)
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
        page.translationX = scroll.toFloat()
    }

    private fun setRotation(page: View, position: Float, percentage: Float) {
        if (position > 0) {
            page.rotationY = -180 * (percentage + 1)
        } else {
            page.rotationY = 180 * (percentage + 1)
        }
    }
}
