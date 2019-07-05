package org.stepik.android.view.step_quiz_text.ui.delegate

import android.support.annotation.DrawableRes
import android.support.v4.widget.TextViewCompat
import android.support.v7.content.res.AppCompatResources
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_step_quiz_text.view.*
import org.stepic.droid.R
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.AppConstants
import org.stepik.android.model.Reply
import org.stepik.android.model.Submission
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.base.ui.drawable.GravityDrawable
import org.stepik.android.view.step_quiz.mapper.StepQuizFormMapper
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate

class TextStepQuizFormDelegate(
    private val stepWrapper: StepPersistentWrapper,
    containerView: View
) : StepQuizFormDelegate {
    private val context = containerView.context

    private val stepQuizFormMapper = StepQuizFormMapper()

    private val textField = containerView.stringStepQuizField as TextView
    private val quizDescription = containerView.stringStepQuizDescription

    init {
        val (inputType, textRes) =
            when (val blockName = stepWrapper.step.block?.name) {
                AppConstants.TYPE_STRING ->
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE to R.string.step_quiz_string_description

                AppConstants.TYPE_NUMBER ->
                    InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED to R.string.step_quiz_number_description

                AppConstants.TYPE_MATH ->
                    InputType.TYPE_CLASS_TEXT to R.string.step_quiz_math_description

                AppConstants.TYPE_FREE_ANSWER ->
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE to R.string.step_quiz_free_answer_description

                else ->
                    throw IllegalArgumentException("Unsupported block type = $blockName")
            }

        textField.inputType = inputType
        quizDescription.setText(textRes)
    }

    override fun createReply(): ReplyResult =
        textField.text.toString().let { value ->
            if (value.isNotEmpty()) {
                val reply =
                    when (stepWrapper.step.block?.name) {
                        AppConstants.TYPE_NUMBER ->
                            Reply(number = value)

                        AppConstants.TYPE_MATH ->
                            Reply(formula = value)

                        else ->
                            Reply(text = value)
                    }
                ReplyResult.Success(reply)
            } else {
                ReplyResult.Error(context.getString(R.string.step_quiz_text_empty_reply))
            }
        }

    override fun setState(state: StepQuizView.State.AttemptLoaded) {
        val submission = (state.submissionState as? StepQuizView.SubmissionState.Loaded)
            ?.submission

        val reply = submission?.reply

        textField.isEnabled = stepQuizFormMapper.isQuizEnabled(state)
        textField.text =
            when (stepWrapper.step.block?.name) {
                AppConstants.TYPE_NUMBER ->
                    reply?.number

                AppConstants.TYPE_MATH ->
                    reply?.formula

                else ->
                    reply?.text
            } ?: ""

        @DrawableRes
        val drawableRes =
            when (submission?.status) {
                Submission.Status.CORRECT ->
                    R.drawable.ic_step_quiz_text_correct

                Submission.Status.WRONG ->
                    R.drawable.ic_step_quiz_text_wrong

                else ->
                    null
            }

        val drawable = drawableRes
            ?.let { AppCompatResources.getDrawable(textField.context, it) }
            ?.let { GravityDrawable(it, Gravity.BOTTOM, textField.resources.getDimensionPixelSize(R.dimen.step_quiz_text_field_min_height)) }

        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(textField, null, null, drawable, null)
    }
}