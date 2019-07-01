package org.stepik.android.view.step_quiz_text.ui.delegate

import android.text.InputType
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_step_quiz_text.view.*
import org.stepic.droid.R
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.ui.util.changeVisibility
import org.stepic.droid.util.AppConstants
import org.stepik.android.model.Reply
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.view.step_quiz.mapper.StepQuizFormMapper
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate

class TextStepQuizFormDelegate(
    stepWrapper: StepPersistentWrapper,
    containerView: View
) : StepQuizFormDelegate {
    private val stepQuizFormMapper = StepQuizFormMapper()

    private val textField = containerView.stringStepQuizField as TextView
    private val quizDescription = containerView.stringStepQuizDescription

    init {
        val (inputType, textRes) =
            when (val blockName = stepWrapper.step.block?.name) {
                AppConstants.TYPE_STRING ->
                    InputType.TYPE_CLASS_TEXT to R.string.step_quiz_string_description

                AppConstants.TYPE_NUMBER ->
                    InputType.TYPE_CLASS_NUMBER to R.string.step_quiz_number_description

                AppConstants.TYPE_MATH ->
                    InputType.TYPE_CLASS_TEXT to R.string.step_quiz_math_description

                AppConstants.TYPE_FREE_ANSWER ->
                    InputType.TYPE_CLASS_TEXT to R.string.step_quiz_free_answer_description

                else ->
                    throw IllegalArgumentException("Unsupported block type = $blockName")
            }

        textField.inputType = inputType
        quizDescription.setText(textRes)
    }

    override fun createReply(): Reply =
        Reply(text = textField.text.toString())

    override fun validateForm(): String? =
        if (textField.text.isEmpty()) {
            textField.context.getString(R.string.empty_courses_anonymous) // todo add string res
        } else {
            null
        }

    override fun setState(state: StepQuizView.State) {
        if (state !is StepQuizView.State.AttemptLoaded) return

        textField.isEnabled = stepQuizFormMapper.isQuizEnabled(state)
        textField.text = (state.submissionState as? StepQuizView.SubmissionState.Loaded)
            ?.submission
            ?.reply
            ?.text
            ?: ""
    }
}