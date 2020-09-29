package org.stepik.android.view.step_quiz_review.ui.delegate

import android.content.Context
import android.view.View
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_step_quiz_review_peer.*
import kotlinx.android.synthetic.main.layout_step_quiz_review_footer.*
import kotlinx.android.synthetic.main.layout_step_quiz_review_header.*
import org.stepic.droid.R
import org.stepic.droid.util.toFixed
import org.stepik.android.model.ReviewStrategyType
import org.stepik.android.model.Submission
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewView
import org.stepik.android.view.step_quiz_review.ui.widget.ReviewStatusView
import ru.nobird.android.core.model.safeCast


class StepQuizReviewPeerDelegate(
    override val containerView: View,
    private val instructionType: ReviewStrategyType
) : LayoutContainer, StepQuizReviewDelegate {
    private val resources = containerView.resources

    override fun render(state: StepQuizReviewView.State) {
        renderStep1(state)
        renderStep2(state)

        if (instructionType == ReviewStrategyType.PEER) {
            renderStep3(state)
            renderStep4(state)
        }

        renderStep5(state)
    }

    private fun renderStep1(state: StepQuizReviewView.State) {
        // todo work with quiz
        when (state) {
            is StepQuizReviewView.State.SubmissionNotMade -> {
                val submissionStatus = state.quizState.safeCast<StepQuizView.State.AttemptLoaded>()
                    ?.submissionState
                    ?.safeCast<StepQuizView.SubmissionState.Loaded>()
                    ?.submission
                    ?.status

                reviewStep1Status.status =
                    if (submissionStatus == Submission.Status.WRONG) {
                        ReviewStatusView.Status.ERROR
                    } else {
                        ReviewStatusView.Status.IN_PROGRESS
                    }

                reviewStep1Title.isEnabled = true
            }

            else -> {
                reviewStep1Title.isEnabled = false
                reviewStep1Status.status = ReviewStatusView.Status.COMPLETED
            }
        }
    }

    private fun renderStep2(state: StepQuizReviewView.State) {
        // todo work with quiz
        when (state) {
            is StepQuizReviewView.State.SubmissionNotMade -> {
                reviewStep2Title.setText(R.string.step_quiz_review_send_pending)
                reviewStep2Title.isEnabled = false
                reviewStep2Link.isEnabled = false
                reviewStep2Status.status = ReviewStatusView.Status.PENDING
            }
            is StepQuizReviewView.State.SubmissionNotSelected -> {
                reviewStep2Title.setText(R.string.step_quiz_review_send_in_progress)
                reviewStep2Title.isEnabled = true
                reviewStep2Link.isEnabled = true
                reviewStep2Status.status = ReviewStatusView.Status.IN_PROGRESS
            }
            else -> {
                reviewStep2Title.setText(R.string.step_quiz_review_send_completed)
                reviewStep2Title.isEnabled = false
                reviewStep2Link.isEnabled = true
                reviewStep2Status.status = ReviewStatusView.Status.COMPLETED
            }
        }
    }

    private fun renderStep3(state: StepQuizReviewView.State) {
        val reviewCount = state.safeCast<StepQuizReviewView.State.WithInstruction>()?.instruction?.minReviews ?: 0

        when (state) {
            is StepQuizReviewView.State.SubmissionNotMade,
            is StepQuizReviewView.State.SubmissionNotSelected,
            is StepQuizReviewView.State.SubmissionSelectedLoading -> {
                reviewStep3Title.text = resources.getQuantityString(R.plurals.step_quiz_review_given_pending, reviewCount, reviewCount)
                reviewStep3Title.isEnabled = false
                reviewStep3Link.isEnabled = false
                reviewStep3Status.status = ReviewStatusView.Status.PENDING
            }
            is StepQuizReviewView.State.SubmissionSelected -> {
                val inProgressText = resources.getQuantityString(R.plurals.step_quiz_review_given_in_progress, reviewCount, reviewCount)
                val text =
                    if (state.session.givenReviews.isNotEmpty()) {
                        inProgressText + resources.getQuantityString(R.plurals.step_quiz_review_given_completed, state.session.givenReviews.size, state.session.givenReviews.size)
                    } else {
                        inProgressText
                    }

                reviewStep3Title.text = text
                reviewStep3Title.isEnabled = true
                reviewStep3Link.isEnabled = true
                reviewStep3Status.status = ReviewStatusView.Status.IN_PROGRESS
            }
            else -> {
                reviewStep3Title.text = resources.getQuantityString(R.plurals.step_quiz_review_given_completed, reviewCount, reviewCount)
                reviewStep3Title.isEnabled = false
                reviewStep3Link.isEnabled = true
                reviewStep3Status.status = ReviewStatusView.Status.COMPLETED
            }
        }
    }

    private fun renderStep4(state: StepQuizReviewView.State) {
        val reviewCount = state.safeCast<StepQuizReviewView.State.WithInstruction>()?.instruction?.minReviews ?: 0

        when (state) {
            is StepQuizReviewView.State.SubmissionNotMade,
            is StepQuizReviewView.State.SubmissionNotSelected,
            is StepQuizReviewView.State.SubmissionSelectedLoading -> {
                reviewStep4Title.text = resources.getQuantityString(R.plurals.step_quiz_review_taken_pending, reviewCount, reviewCount)
                reviewStep4Title.isEnabled = false
                reviewStep4Link.isEnabled = false
                reviewStep4Status.status = ReviewStatusView.Status.PENDING
            }
            is StepQuizReviewView.State.SubmissionSelected -> {
                val inProgressText = resources.getQuantityString(R.plurals.step_quiz_review_taken_in_progress, reviewCount, reviewCount)
                val text =
                    if (state.session.takenReviews.isNotEmpty()) {
                        inProgressText + resources.getQuantityString(R.plurals.step_quiz_review_taken_completed, state.session.takenReviews.size, state.session.takenReviews.size)
                    } else {
                        inProgressText
                    }

                reviewStep4Title.text = text
                reviewStep4Title.isEnabled = true
                reviewStep4Link.isEnabled = true
                reviewStep4Status.status = ReviewStatusView.Status.IN_PROGRESS
            }
            else -> {
                reviewStep4Title.text = resources.getQuantityString(R.plurals.step_quiz_review_taken_completed, reviewCount, reviewCount)
                reviewStep4Title.isEnabled = false
                reviewStep4Link.isEnabled = true
                reviewStep4Status.status = ReviewStatusView.Status.COMPLETED
            }
        }
    }

    private fun renderStep5(state: StepQuizReviewView.State) {
        when (state) {
            is StepQuizReviewView.State.Completed -> {
                val receivedPoints = state.progress.score?.toFloatOrNull() ?: 0f


                reviewStep5Title.text =
                    resolveQuantityString(
                        containerView.context,
                        receivedPoints,
                        state.progress.cost,
                        R.string.step_quiz_review_peer_completed,
                        R.string.step_quiz_review_peer_completed,
                        R.plurals.points
                    )
                reviewStep5Title.isEnabled = true
                reviewStep5Link.isEnabled = true
                reviewStep5Status.status = ReviewStatusView.Status.COMPLETED
            }
            // todo handle instructor
            else -> {
                reviewStep5Title.text = resources.getString(R.string.step_quiz_review_peer_pending, resources.getQuantityString(R.plurals.points, 0, 0))
                reviewStep5Title.isEnabled = false
                reviewStep5Link.isEnabled = false
                reviewStep5Status.status = ReviewStatusView.Status.PENDING
            }
        }
    }

    private fun resolveQuantityString(context: Context, stepScore: Float, stepCost: Long, @StringRes stringRes: Int, @StringRes fractionRes: Int, @PluralsRes pluralRes: Int): String =
        if (stepScore.toLong() == 0L) {
            context.getString(fractionRes, stepScore.toFixed(context.resources.getInteger(R.integer.score_decimal_count)), stepCost)
        } else {
            context.getString(stringRes, context.resources.getQuantityString(pluralRes, stepScore.toInt(), stepScore.toFixed(context.resources.getInteger(R.integer.score_decimal_count))), stepCost)
        }
}