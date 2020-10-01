package org.stepik.android.view.step_quiz_review.ui.delegate

import android.view.View
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_step_quiz_review_peer.*
import kotlinx.android.synthetic.main.layout_step_quiz_review_footer.*
import kotlinx.android.synthetic.main.layout_step_quiz_review_header.*
import org.stepic.droid.R
import org.stepik.android.model.ReviewStrategyType
import org.stepik.android.model.Submission
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewView
import org.stepik.android.view.progress.ui.mapper.ProgressTextMapper
import org.stepik.android.view.step_quiz_review.ui.widget.ReviewStatusView
import ru.nobird.android.core.model.safeCast

class StepQuizReviewDelegate(
    override val containerView: View,
    private val instructionType: ReviewStrategyType,
    private val actionListener: ActionListener
) : LayoutContainer {
    private val resources = containerView.resources

    init {
        if (instructionType == ReviewStrategyType.PEER) {
            reviewStep3Container.setOnClickListener { actionListener.onStartReviewClicked() }
        }
    }

    fun render(state: StepQuizReviewView.State) {
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
                setStepStatus(reviewStep2Title, reviewStep2Link, reviewStep2Status, ReviewStatusView.Status.PENDING)
            }
            is StepQuizReviewView.State.SubmissionNotSelected -> {
                reviewStep2Title.setText(R.string.step_quiz_review_send_in_progress)
                setStepStatus(reviewStep2Title, reviewStep2Link, reviewStep2Status, ReviewStatusView.Status.IN_PROGRESS)
            }
            else -> {
                reviewStep2Title.setText(R.string.step_quiz_review_send_completed)
                setStepStatus(reviewStep2Title, reviewStep2Link, reviewStep2Status, ReviewStatusView.Status.COMPLETED)
            }
        }
    }

    private fun renderStep3(state: StepQuizReviewView.State) {
        val reviewCount = state.safeCast<StepQuizReviewView.State.WithInstruction>()?.instruction?.minReviews ?: 0

        when (state) {
            is StepQuizReviewView.State.SubmissionNotMade,
            is StepQuizReviewView.State.SubmissionNotSelected,
            is StepQuizReviewView.State.SubmissionSelectedLoading -> {
                reviewStep3Title.setText(R.string.step_quiz_review_given_pending_zero)
                setStepStatus(reviewStep3Title, reviewStep3Link, reviewStep3Status, ReviewStatusView.Status.PENDING)
                reviewStep3Container.isVisible = false
                reviewStep3Loading.isVisible = false
            }
            is StepQuizReviewView.State.SubmissionSelected -> {
                val givenReviewCount = state.session.givenReviews.size
                val remainingReviewCount = reviewCount - givenReviewCount

                val text =
                    buildString {
                        if (remainingReviewCount > 0) {
                            @PluralsRes
                            val pluralRes =
                                if (givenReviewCount > 0) {
                                    R.plurals.step_quiz_review_given_in_progress
                                } else {
                                    R.plurals.step_quiz_review_given_pending
                                }
                            append(resources.getQuantityString(pluralRes, remainingReviewCount, remainingReviewCount))
                        }

                        if (givenReviewCount > 0) {
                            if (isNotEmpty()) {
                                append(" ")
                            }
                            append(resources.getQuantityString(R.plurals.step_quiz_review_given_completed, givenReviewCount, givenReviewCount))
                        }
                    }

                reviewStep3Title.text = text

                reviewStep3Container.isVisible = remainingReviewCount > 0 && !state.isReviewCreationInProgress

                if (reviewStep3Container.isVisible) {
                    reviewStep3Container.isEnabled = remainingReviewCount <= 0 || state.session.isReviewAvailable
                    reviewStep3Container.setText(if (reviewStep3Container.isEnabled) R.string.step_quiz_review_given_start_review else R.string.step_quiz_review_given_no_review)
                }

                reviewStep3Loading.isVisible = state.isReviewCreationInProgress
                setStepStatus(reviewStep3Title, reviewStep3Link, reviewStep3Status, ReviewStatusView.Status.IN_PROGRESS)
            }
            is StepQuizReviewView.State.Completed -> {
                val givenReviewCount = state.session.givenReviews.size

                reviewStep3Title.text = resources.getQuantityString(R.plurals.step_quiz_review_given_completed, givenReviewCount, givenReviewCount)
                reviewStep3Container.isVisible = false
                reviewStep3Loading.isVisible = false
                setStepStatus(reviewStep3Title, reviewStep3Link, reviewStep3Status, ReviewStatusView.Status.COMPLETED)
            }
        }
    }

    private fun renderStep4(state: StepQuizReviewView.State) {
        val reviewCount = state.safeCast<StepQuizReviewView.State.WithInstruction>()?.instruction?.minReviews ?: 0

        when (state) {
            is StepQuizReviewView.State.SubmissionNotMade,
            is StepQuizReviewView.State.SubmissionNotSelected,
            is StepQuizReviewView.State.SubmissionSelectedLoading -> {
                reviewStep4Title.setText(R.string.step_quiz_review_taken_pending_zero)
                setStepStatus(reviewStep4Title, reviewStep4Link, reviewStep4Status, ReviewStatusView.Status.PENDING)
                reviewStep4Container.isVisible = false
            }
            is StepQuizReviewView.State.SubmissionSelected -> {
                val takenReviewCount = state.session.takenReviews.size
                val remainingReviewCount = reviewCount - takenReviewCount

                val text =
                    buildString {
                        if (remainingReviewCount > 0) {
                            @PluralsRes
                            val pluralRes =
                                if (takenReviewCount > 0) {
                                    R.plurals.step_quiz_review_taken_in_progress
                                } else {
                                    R.plurals.step_quiz_review_taken_pending
                                }
                            append(resources.getQuantityString(pluralRes, remainingReviewCount, remainingReviewCount))
                        }

                        if (takenReviewCount > 0) {
                            if (isNotEmpty()) {
                                append(" ")
                            }
                            append(resources.getQuantityString(R.plurals.step_quiz_review_taken_completed, takenReviewCount, takenReviewCount))
                        }
                    }

                val status =
                    if (remainingReviewCount > 0) {
                        ReviewStatusView.Status.IN_PROGRESS
                    } else {
                        ReviewStatusView.Status.COMPLETED
                    }

                reviewStep4Title.text = text
                setStepStatus(reviewStep4Title, reviewStep4Link, reviewStep4Status, status)

                reviewStep4Container.isVisible = takenReviewCount > 0
                reviewStep4Container.setOnClickListener { actionListener.onTakenReviewClicked(state.session.id) }
            }
            is StepQuizReviewView.State.Completed -> {
                val takenReviewCount = state.session.takenReviews.size
                reviewStep4Title.text = resources.getQuantityString(R.plurals.step_quiz_review_taken_completed, takenReviewCount, takenReviewCount)
                setStepStatus(reviewStep4Title, reviewStep4Link, reviewStep4Status, ReviewStatusView.Status.COMPLETED)
                reviewStep4Container.isVisible = takenReviewCount > 0
                reviewStep4Container.setOnClickListener { actionListener.onTakenReviewClicked(state.session.id) }
            }
        }
    }

    private fun renderStep5(state: StepQuizReviewView.State) {
        reviewStep5Status.position =
            when (instructionType) {
                ReviewStrategyType.PEER -> 5
                ReviewStrategyType.INSTRUCTOR -> 3
            }

        when (state) {
            is StepQuizReviewView.State.Completed -> {
                val receivedPoints = state.progress?.score?.toFloatOrNull() ?: 0f

                reviewStep5Title.text = ProgressTextMapper
                    .mapProgressToText(
                        containerView.context,
                        receivedPoints,
                        state.progress?.cost ?: 0,
                        R.string.step_quiz_review_peer_completed,
                        R.string.step_quiz_review_peer_completed,
                        R.plurals.points
                    )

                when (instructionType) {
                    ReviewStrategyType.PEER ->
                        reviewStep5Container.isVisible = false

                    ReviewStrategyType.INSTRUCTOR -> {
                        reviewStep5Container.setOnClickListener { actionListener.onTakenReviewClicked(state.session.id) }
                        reviewStep5Container.isVisible = true
                    }
                }
                setStepStatus(reviewStep5Title, reviewStep5Link, reviewStep5Status, ReviewStatusView.Status.IN_PROGRESS)
                reviewStep5Status.status = ReviewStatusView.Status.COMPLETED
            }
            else -> {
                val cost = state.safeCast<StepQuizReviewView.State.WithProgress>()?.progress?.cost ?: 0L

                @StringRes
                val stringRes =
                    when (instructionType) {
                        ReviewStrategyType.PEER ->
                            R.string.step_quiz_review_peer_pending

                        ReviewStrategyType.INSTRUCTOR ->
                            R.string.step_quiz_review_instructor_pending
                    }

                reviewStep5Title.text = resources.getString(stringRes, resources.getQuantityString(R.plurals.points, cost.toInt(), cost))
                reviewStep5Container.isVisible = false
                val status =
                    if (state is StepQuizReviewView.State.SubmissionSelected && instructionType == ReviewStrategyType.INSTRUCTOR) {
                        ReviewStatusView.Status.IN_PROGRESS
                    } else {
                        ReviewStatusView.Status.PENDING
                    }

                setStepStatus(reviewStep5Title, reviewStep5Link, reviewStep5Status, status)
            }
        }
    }

    private fun setStepStatus(titleView: View, linkView: View, statusView: ReviewStatusView, status: ReviewStatusView.Status) {
        titleView.isEnabled = status == ReviewStatusView.Status.IN_PROGRESS
        linkView.isEnabled = status.ordinal >= ReviewStatusView.Status.IN_PROGRESS.ordinal
        statusView.status = status
    }

    interface ActionListener {
        fun onSelectDifferentSubmissionClicked()

        fun onStartReviewClicked()
        fun onTakenReviewClicked(sessionId: Long)
    }
}