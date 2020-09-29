package org.stepik.android.view.step_quiz_review.ui.delegate

import org.stepik.android.presentation.step_quiz_review.StepQuizReviewView

interface StepQuizReviewDelegate {
    fun render(state: StepQuizReviewView.State)
}