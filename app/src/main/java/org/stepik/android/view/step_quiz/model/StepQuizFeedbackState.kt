package org.stepik.android.view.step_quiz.model

sealed class StepQuizFeedbackState {
    object Idle : StepQuizFeedbackState()
    data class Correct(val hint: String?, val isFreeAnswer: Boolean = false) : StepQuizFeedbackState()
    data class PartiallyCorrect(val hint: String?) : StepQuizFeedbackState()
    data class Wrong(val hint: String?, val isLastTry: Boolean = false) : StepQuizFeedbackState()
    object Evaluation : StepQuizFeedbackState()
    data class Validation(val message: String) : StepQuizFeedbackState()
}