package org.stepik.android.view.step.ui.delegate

import android.view.Gravity
import android.view.ViewGroup
import androidx.core.view.isVisible
import org.stepic.droid.R
import org.stepic.droid.databinding.ViewStepNavigationBinding
import org.stepik.android.domain.step.model.StepNavigationDirection

class StepNavigationDelegate(
    containerView: android.view.View,
    private val onDirectionClicked: (StepNavigationDirection) -> Unit
) {
    private val binding = ViewStepNavigationBinding.bind(containerView)
    private val nextButton = binding.stepNavigationNext
    private val prevButton = binding.stepNavigationPrev

    init {
        binding.root.isVisible = false

        prevButton.setOnClickListener { onDirectionClicked(StepNavigationDirection.PREV) }
        nextButton.setOnClickListener { onDirectionClicked(StepNavigationDirection.NEXT) }
    }

    fun setState(directions: Set<StepNavigationDirection>) {
        binding.root.isVisible = directions.isNotEmpty()

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
