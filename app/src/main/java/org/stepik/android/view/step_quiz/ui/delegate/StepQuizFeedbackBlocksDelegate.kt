package org.stepik.android.view.step_quiz.ui.delegate

import android.graphics.drawable.AnimationDrawable
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v7.content.res.AppCompatResources
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.layout_step_quiz_feedback_block.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.setCompoundDrawables
import org.stepik.android.view.step_quiz.model.StepQuizFeedbackState
import org.stepik.android.view.ui.delegate.ViewStateDelegate

class StepQuizFeedbackBlocksDelegate(
    containerView: View
) {
    private val context = containerView.context
    private val resources = containerView.resources

    private val stepQuizFeedbackEvaluation = containerView.stepQuizFeedbackEvaluation
    private val stepQuizFeedbackCorrect = containerView.stepQuizFeedbackCorrect
    private val stepQuizFeedbackWrong = containerView.stepQuizFeedbackWrong
    private val stepQuizFeedbackValidation = containerView.stepQuizFeedbackValidation

    private val stepQuizFeedbackHint = containerView.stepQuizFeedbackHint

    private val viewStateDelegate = ViewStateDelegate<StepQuizFeedbackState>()

    init {
        viewStateDelegate.addState<StepQuizFeedbackState.Idle>()
        viewStateDelegate.addState<StepQuizFeedbackState.Evaluation>(stepQuizFeedbackEvaluation)
        viewStateDelegate.addState<StepQuizFeedbackState.Correct>(stepQuizFeedbackCorrect, stepQuizFeedbackHint)
        viewStateDelegate.addState<StepQuizFeedbackState.Wrong>(stepQuizFeedbackWrong, stepQuizFeedbackHint)
        viewStateDelegate.addState<StepQuizFeedbackState.Validation>(stepQuizFeedbackValidation)

        val drawable = AppCompatResources.getDrawable(context, R.drawable.ic_step_quiz_evaluation) as? AnimationDrawable
        stepQuizFeedbackEvaluation.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        drawable?.start()

        stepQuizFeedbackCorrect.setCompoundDrawables(start = R.drawable.ic_step_quiz_correct)
        stepQuizFeedbackCorrect.text = resources.getStringArray(R.array.step_quiz_feedback_correct).random()

        stepQuizFeedbackWrong.setCompoundDrawables(start = R.drawable.ic_step_quiz_wrong)
        stepQuizFeedbackWrong.setText(R.string.step_quiz_feedback_wrong_not_last_try)

        stepQuizFeedbackValidation.setCompoundDrawables(start = R.drawable.ic_step_quiz_validation)
        stepQuizFeedbackValidation.setText(R.string.step_quiz_feedback_validation_fill_blanks)
    }

    fun setState(state: StepQuizFeedbackState) {
        viewStateDelegate.switchState(state)
        when (state) {
            is StepQuizFeedbackState.Correct -> {
                stepQuizFeedbackCorrect.text =
                    if (state.isFreeAnswer) {
                        context.getString(R.string.step_quiz_feedback_correct_free_answer)
                    } else {
                        resources.getStringArray(R.array.step_quiz_feedback_correct).random()
                    }
                setHint(stepQuizFeedbackCorrect, R.drawable.bg_step_quiz_feedback_correct, R.drawable.bg_step_quiz_feedback_correct_with_hint, state.hint)
            }

            is StepQuizFeedbackState.Wrong -> {
                @StringRes
                val stringRes =
                    if (state.isLastTry) {
                        R.string.step_quiz_feedback_wrong_last_try
                    } else {
                        R.string.step_quiz_feedback_wrong_not_last_try
                    }
                stepQuizFeedbackWrong.setText(stringRes)
                setHint(stepQuizFeedbackWrong, R.drawable.bg_step_quiz_feedback_wrong, R.drawable.bg_step_quiz_feedback_wrong_with_hint, state.hint)
            }

            is StepQuizFeedbackState.Validation ->
                stepQuizFeedbackValidation.text = state.message
        }
    }

    private fun setHint(
        targetView: TextView,
        @DrawableRes backgroundRes: Int, @DrawableRes hintedBackgroundRes: Int,
        hint: String?
    ) {
        if (hint != null) {
            targetView.setBackgroundResource(hintedBackgroundRes)
            stepQuizFeedbackHint.text = hint
            stepQuizFeedbackHint.visibility = View.VISIBLE
        } else {
            targetView.setBackgroundResource(backgroundRes)
            stepQuizFeedbackHint.visibility = View.GONE
        }
    }
}