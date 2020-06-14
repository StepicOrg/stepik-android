package org.stepik.android.view.step.ui.delegate

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.view_step_navigation.view.*
import org.stepic.droid.R
import org.stepik.android.domain.step.model.StepNavigationDirection

class StepNavigationDelegate(
    private val containerView: View,
    private val onDirectionClicked: (StepNavigationDirection) -> Unit
) {
    private val nextButton = containerView.stepNavigationNext
    private val prevButton = containerView.stepNavigationPrev

    init {
        containerView.isVisible = false

        prevButton.setOnClickListener { onDirectionClicked(StepNavigationDirection.PREV) }
        nextButton.setOnClickListener { onDirectionClicked(StepNavigationDirection.NEXT) }
    }

    fun setState(directions: Set<StepNavigationDirection>) {
        containerView.isVisible = directions.isNotEmpty()

        val isPrevAvailable = StepNavigationDirection.PREV in directions
        val isNextAvailable = StepNavigationDirection.NEXT in directions

        prevButton.isVisible = isPrevAvailable
        nextButton.isVisible = isNextAvailable

        when {
            !isPrevAvailable && isNextAvailable -> {
                nextButton.gravity = Gravity.CENTER
            }

            isPrevAvailable && !isNextAvailable -> {
                prevButton.setText(R.string.step_navigation_prev)
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