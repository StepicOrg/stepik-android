package org.stepik.android.view.step.ui.delegate

import android.view.Gravity
import android.view.ViewGroup
import androidx.core.view.isVisible
import org.stepic.droid.R
import org.stepic.droid.databinding.ViewStepNavigationBinding
import org.stepik.android.domain.step.model.StepNavigationDirection

class StepNavigationDelegate(
    private val binding: ViewStepNavigationBinding,
    private val onDirectionClicked: (StepNavigationDirection) -> Unit
) {
    init {
        binding.root.isVisible = false

        binding.stepNavigationNext.setOnClickListener { onDirectionClicked(StepNavigationDirection.PREV) }
        binding.stepNavigationNext.setOnClickListener { onDirectionClicked(StepNavigationDirection.NEXT) }
    }

    fun setState(directions: Set<StepNavigationDirection>) {
        binding.root.isVisible = directions.isNotEmpty()

        val isPrevAvailable = StepNavigationDirection.PREV in directions
        val isNextAvailable = StepNavigationDirection.NEXT in directions

        binding.stepNavigationNext.isVisible = isPrevAvailable
        binding.stepNavigationNext.isVisible = isNextAvailable

        when {
            !isPrevAvailable && isNextAvailable -> {
                binding.stepNavigationNext.gravity = Gravity.CENTER
            }

            isPrevAvailable && !isNextAvailable -> {
                binding.stepNavigationNext.setText(R.string.step_navigation_prev)
                binding.stepNavigationNext.layoutParams = binding.stepNavigationNext.layoutParams.apply { width = 0 }
                binding.stepNavigationNext.compoundDrawablePadding =
                    binding.stepNavigationNext.compoundDrawablePadding
            }

            isPrevAvailable && isNextAvailable -> {
                binding.stepNavigationNext.text = null
                binding.stepNavigationNext.layoutParams = binding.stepNavigationNext.layoutParams.apply {
                    width = ViewGroup.LayoutParams.WRAP_CONTENT
                }
                binding.stepNavigationNext.compoundDrawablePadding = 0
                binding.stepNavigationNext.gravity = Gravity.CENTER_VERTICAL or Gravity.START
            }
        }
    }
}
