package org.stepik.android.view.step_quiz.ui.delegate

import android.graphics.PorterDuff
import android.graphics.drawable.AnimationDrawable
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.layout_step_quiz_feedback_block.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.util.setTextViewBackgroundWithoutResettingPadding
import org.stepic.droid.util.getDrawableCompat
import org.stepik.android.view.step_quiz.model.StepQuizFeedbackState
import org.stepik.android.view.ui.delegate.ViewStateDelegate

class StepQuizFeedbackBlocksDelegate(
    containerView: View,
    private val hasReview: Boolean,
    private val onReviewClicked: () -> Unit
) {
    companion object {
        private const val EVALUATION_FRAME_DURATION_MS = 250
    }

    private val context = containerView.context
    private val resources = containerView.resources

    private val stepQuizFeedbackEvaluation = containerView.stepQuizFeedbackEvaluation
    private val stepQuizFeedbackCorrect = containerView.stepQuizFeedbackCorrect
    private val stepQuizFeedbackPartiallyCorrect = containerView.stepQuizFeedbackPartiallyCorrect
    private val stepQuizFeedbackWrong = containerView.stepQuizFeedbackWrong
    private val stepQuizFeedbackValidation = containerView.stepQuizFeedbackValidation

    private val stepQuizFeedbackHint = containerView.stepQuizFeedbackHint

    private val viewStateDelegate = ViewStateDelegate<StepQuizFeedbackState>()

    init {
        viewStateDelegate.addState<StepQuizFeedbackState.Idle>()
        viewStateDelegate.addState<StepQuizFeedbackState.Evaluation>(containerView, stepQuizFeedbackEvaluation)
        viewStateDelegate.addState<StepQuizFeedbackState.Correct>(containerView, stepQuizFeedbackCorrect, stepQuizFeedbackHint)
        viewStateDelegate.addState<StepQuizFeedbackState.PartiallyCorrect>(containerView, stepQuizFeedbackPartiallyCorrect, stepQuizFeedbackHint)
        viewStateDelegate.addState<StepQuizFeedbackState.Wrong>(containerView, stepQuizFeedbackWrong, stepQuizFeedbackHint)
        viewStateDelegate.addState<StepQuizFeedbackState.Validation>(containerView, stepQuizFeedbackValidation)

        val evaluationDrawable = AnimationDrawable()
        evaluationDrawable.addFrame(context.getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_1), EVALUATION_FRAME_DURATION_MS)
        evaluationDrawable.addFrame(context.getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_2), EVALUATION_FRAME_DURATION_MS)
        evaluationDrawable.addFrame(context.getDrawableCompat(R.drawable.ic_step_quiz_evaluation_frame_3), EVALUATION_FRAME_DURATION_MS)
        evaluationDrawable.isOneShot = false

        stepQuizFeedbackEvaluation.setCompoundDrawablesWithIntrinsicBounds(evaluationDrawable, null, null, null)
        evaluationDrawable.start()

        if (hasReview) {
            stepQuizFeedbackCorrect.text = context.getString(R.string.review_warning)
            stepQuizFeedbackCorrect.setOnClickListener { onReviewClicked() }
        } else {
            stepQuizFeedbackCorrect.text = resources.getStringArray(R.array.step_quiz_feedback_correct).random()
        }
        // todo fix ripple
        stepQuizFeedbackCorrect.setTextViewBackgroundWithoutResettingPadding(R.drawable.bg_shape_rounded)

        stepQuizFeedbackWrong.setText(R.string.step_quiz_feedback_wrong_not_last_try)

        stepQuizFeedbackHint.background = AppCompatResources
            .getDrawable(context, R.drawable.bg_shape_rounded_bottom)
            ?.mutate()
            ?.let { DrawableCompat.wrap(it) }
            ?.also {
                DrawableCompat.setTint(it, ContextCompat.getColor(context, R.color.color_elevation_overlay_1dp))
                DrawableCompat.setTintMode(it, PorterDuff.Mode.SRC_IN)
            }
    }

    fun setState(state: StepQuizFeedbackState) {
        viewStateDelegate.switchState(state)
        when (state) {
            is StepQuizFeedbackState.Correct -> {
                stepQuizFeedbackCorrect.text =
                    when {
                        hasReview ->
                            context.getString(R.string.review_warning)
                        state.isFreeAnswer ->
                            context.getString(R.string.step_quiz_feedback_correct_free_answer)
                        else ->
                            resources.getStringArray(R.array.step_quiz_feedback_correct).random()
                    }
                setHint(stepQuizFeedbackCorrect, state.hint)
            }

            is StepQuizFeedbackState.PartiallyCorrect -> {
                stepQuizFeedbackPartiallyCorrect.text = resources.getStringArray(R.array.step_quiz_feedback_partially_correct).random()
                setHint(stepQuizFeedbackPartiallyCorrect, state.hint)
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
                setHint(stepQuizFeedbackWrong, state.hint)
            }

            is StepQuizFeedbackState.Validation ->
                stepQuizFeedbackValidation.text = state.message
        }
    }

    private fun setHint(
        targetView: TextView,
        hint: String?
    ) {
        stepQuizFeedbackHint.isVisible = hint != null
        stepQuizFeedbackHint.setText(hint)
        val backgroundShape =
            if (hint != null) {
                R.drawable.bg_shape_rounded_top
            } else {
                R.drawable.bg_shape_rounded
            }
        targetView.setTextViewBackgroundWithoutResettingPadding(backgroundShape)
    }
}