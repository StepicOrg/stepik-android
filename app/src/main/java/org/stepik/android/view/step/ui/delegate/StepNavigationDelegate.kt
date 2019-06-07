package org.stepik.android.view.step.ui.delegate

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_step_navigation.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.ui.util.setCompoundDrawables

class StepNavigationDelegate(
    private val containerView: View
) {
    private val nextButton = containerView.stepNavigationNext
    private val prevButton = containerView.stepNavigationPrev

    init {
        containerView.changeVisibility(false)

        nextButton.setCompoundDrawables(end = R.drawable.ic_step_navigation_next)
        prevButton.setCompoundDrawables(start = R.drawable.ic_step_navigation_prev)

        prevButton.setOnClickListener { setState(true, nextButton.visibility != View.VISIBLE) }
        nextButton.setOnClickListener { setState(prevButton.visibility != View.VISIBLE, true) }
    }

    fun setState(isPrevAvailable: Boolean, isNextAvailable: Boolean) {
        containerView.changeVisibility(isPrevAvailable || isNextAvailable)
        prevButton.changeVisibility(isPrevAvailable)
        nextButton.changeVisibility(isNextAvailable)

        when {
            !isPrevAvailable && isNextAvailable -> {
                nextButton.gravity = Gravity.CENTER
            }

            isPrevAvailable && !isNextAvailable -> {
                prevButton.setText(R.string.previous_lesson)
                prevButton.layoutParams = prevButton.layoutParams.apply { width = 0 }
                prevButton.compoundDrawablePadding = nextButton.compoundDrawablePadding
            }

            isPrevAvailable && isNextAvailable -> {
                prevButton.text = null
                prevButton.layoutParams = prevButton.layoutParams.apply { width = ViewGroup.LayoutParams.WRAP_CONTENT }
                prevButton.compoundDrawablePadding = 0
                nextButton.gravity = Gravity.CENTER_VERTICAL or Gravity.START
            }
        }
    }
}