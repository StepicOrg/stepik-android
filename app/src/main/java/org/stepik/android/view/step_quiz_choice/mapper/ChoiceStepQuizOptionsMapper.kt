package org.stepik.android.view.step_quiz_choice.mapper

import org.stepik.android.model.Submission
import org.stepik.android.model.feedback.ChoiceFeedback
import org.stepik.android.view.step_quiz_choice.model.Choice

class ChoiceStepQuizOptionsMapper {
    fun mapChoices(options: List<String>, choices: List<Boolean>?, submission: Submission?, isQuizEnabled: Boolean): List<Choice> {
        val feedback = submission?.feedback as? ChoiceFeedback

        return options.mapIndexed { i, option ->
                val isCorrect =
                    if (choices?.getOrNull(i) == true) {
                        when (submission?.status) {
                            Submission.Status.CORRECT -> true
                            Submission.Status.WRONG -> false
                            else -> null
                        }
                    } else {
                        null
                    }
                Choice(
                    option = option,
                    feedback = feedback?.optionsFeedback?.getOrNull(i),
                    correct = isCorrect,
                    isEnabled = isQuizEnabled
                )
            }
    }
}