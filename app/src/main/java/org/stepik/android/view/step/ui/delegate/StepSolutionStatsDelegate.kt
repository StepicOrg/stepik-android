package org.stepik.android.view.step.ui.delegate

import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import org.stepic.droid.R
import org.stepic.droid.databinding.ViewStepSolutionStatsBinding
import org.stepik.android.model.Step

class StepSolutionStatsDelegate(
    private val binding: ViewStepSolutionStatsBinding,
    step: Step,
    isHasQuiz: Boolean
) {
    private val context = binding.root.context

    init {
        val correctPercentage = step.correctRatio?.let { (it * 100).toInt() } ?: 0

        if (isHasQuiz && correctPercentage > 0) {
            binding.root.isVisible = true

            binding.stepAmountPassed.text = buildSpannedString {
                append(context.resources.getString(R.string.step_amount_passed))
                bold { append(step.passedBy.toString()) }
            }

            binding.stepSolvedPercentage.text = buildSpannedString {
                append(context.resources.getString(R.string.step_correct_submissions_percentage))
                bold {
                    append(context.resources.getString(R.string.percent_symbol, correctPercentage))
                }
            }
        } else {
            binding.root.isVisible = false
        }
    }
}
