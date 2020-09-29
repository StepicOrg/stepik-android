package org.stepik.android.view.step_quiz_review.ui.delegate

import android.view.View
import kotlinx.android.extensions.LayoutContainer
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewView

class StepQuizReviewPeerDelegate(
    override val containerView: View
) : LayoutContainer, StepQuizReviewDelegate {
    override fun render(state: StepQuizReviewView.State) {

    }
}