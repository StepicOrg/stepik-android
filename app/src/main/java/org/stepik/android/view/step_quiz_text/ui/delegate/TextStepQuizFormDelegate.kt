package org.stepik.android.view.step_quiz_text.ui.delegate

import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.TextViewCompat
import kotlinx.android.synthetic.main.fragment_step_quiz.view.*
import kotlinx.android.synthetic.main.layout_step_quiz_text.view.*
import org.stepic.droid.R
import org.stepic.droid.util.AppConstants
import org.stepik.android.model.Reply
import org.stepik.android.model.Submission
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.base.ui.drawable.GravityDrawable
import org.stepik.android.view.step_quiz.resolver.StepQuizFormResolver
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate

class TextStepQuizFormDelegate(
    containerView: View,
    private val stepBlockName: String?
) : StepQuizFormDelegate {
    companion object {
        private const val MINUS = "-\\\u002D\u00AD\u2012\u2013\u2014\u2015\u02D7"
        private const val PLUS = "+"
        private const val POINT = ",\\."
        private const val EXP = "eEеЕ"

        private const val NUMBER_VALIDATION_REGEX = "^[$MINUS$PLUS]?[0-9]*[$POINT]?[0-9]+([$EXP][$$MINUS$PLUS]?[0-9]+)?$"
    }

    private val context = containerView.context

    private val quizTextField = containerView.stringStepQuizField as TextView
    private val quizDescription = containerView.stepQuizDescription

    init {
        val (inputType, @StringRes descriptionTextRes) =
            when (val blockName = stepBlockName) {
                AppConstants.TYPE_STRING ->
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE to R.string.step_quiz_string_description

                AppConstants.TYPE_NUMBER ->
                    InputType.TYPE_CLASS_TEXT to R.string.step_quiz_number_description

                AppConstants.TYPE_MATH ->
                    InputType.TYPE_CLASS_TEXT to R.string.step_quiz_math_description

                AppConstants.TYPE_FREE_ANSWER ->
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE to R.string.step_quiz_free_answer_description

                else ->
                    throw IllegalArgumentException("Unsupported block type = $blockName")
            }

        quizTextField.inputType = inputType
        quizDescription.setText(descriptionTextRes)
    }

    override fun createReply(): ReplyResult =
        quizTextField.text.toString().let { value ->
            if (value.isNotEmpty()) {
                when (stepBlockName) {
                    AppConstants.TYPE_NUMBER ->
                        if (value.matches(NUMBER_VALIDATION_REGEX.toRegex())) {
                            ReplyResult.Success(Reply(number = value))
                        } else {
                            ReplyResult.Error(context.getString(R.string.step_quiz_text_invalid_number_reply))
                        }

                    AppConstants.TYPE_MATH ->
                        ReplyResult.Success(Reply(formula = value))

                    else ->
                        ReplyResult.Success(Reply(text = value))
                }
            } else {
                ReplyResult.Error(context.getString(R.string.step_quiz_text_empty_reply))
            }
        }

    override fun setState(state: StepQuizFeature.State.AttemptLoaded) {
        val submission = (state.submissionState as? StepQuizFeature.SubmissionState.Loaded)
            ?.submission

        val reply = submission?.reply

        quizTextField.isEnabled = StepQuizFormResolver.isQuizEnabled(state)
        quizTextField.text =
            when (stepBlockName) {
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
                    R.drawable.ic_step_quiz_wrong

                else ->
                    null
            }

        val drawable = drawableRes
            ?.let { AppCompatResources.getDrawable(quizTextField.context, it) }
            ?.let { GravityDrawable(it, Gravity.BOTTOM, quizTextField.resources.getDimensionPixelSize(R.dimen.step_quiz_text_field_min_height)) }

        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(quizTextField, null, null, drawable, null)
    }
}