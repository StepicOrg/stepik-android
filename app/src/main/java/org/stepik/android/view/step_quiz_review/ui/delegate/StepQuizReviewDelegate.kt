package org.stepik.android.view.step_quiz_review.ui.delegate

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import org.stepic.droid.R
import org.stepik.android.model.ReviewStrategyType
import org.stepik.android.model.Submission
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.presentation.step_quiz_review.StepQuizReviewFeature
import org.stepik.android.view.progress.ui.mapper.ProgressTextMapper
import org.stepik.android.view.step_quiz.mapper.StepQuizFeedbackMapper
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizDelegate
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFeedbackBlocksDelegate
import org.stepik.android.view.step_quiz_review.ui.widget.ReviewStatusView
import org.stepik.android.view.ui.delegate.ViewStateDelegate
import ru.nobird.app.core.model.safeCast

class StepQuizReviewDelegate(
    containerView: View,
    private val instructionType: ReviewStrategyType,
    private val actionListener: ActionListener,

    private val blockName: String?,
    private val quizView: View,
    private val quizDelegate: StepQuizDelegate,
    private val quizFeedbackBlocksDelegate: StepQuizFeedbackBlocksDelegate
) {
    private val resources = containerView.resources

    private val stepQuizNetworkError = containerView.findViewById<View>(R.id.stepQuizNetworkError)
    private val stepQuizProgress = containerView.findViewById<View>(R.id.stepQuizProgress)
    private val stepQuizDescription = containerView.findViewById<TextView>(R.id.stepQuizDescription)
    private val quizFeedbackView = containerView.findViewById<View>(R.id.quizFeedbackView)

    private val reviewStep1DividerBottom = containerView.findViewById<View>(R.id.reviewStep1DividerBottom)
    private val reviewStep1Container = containerView.findViewById<ViewGroup>(R.id.reviewStep1Container)
    private val reviewStep1Discounting = containerView.findViewById<View>(R.id.reviewStep1Discounting)
    private val reviewStep1QuizContainer = containerView.findViewById<ViewGroup>(R.id.reviewStep1QuizContainer)
    private val reviewStep1ActionButton = containerView.findViewById<View>(R.id.reviewStep1ActionButton)
    private val reviewStep1ActionRetry = containerView.findViewById<View>(R.id.reviewStep1ActionRetry)
    private val reviewStep1Status = containerView.findViewById<ReviewStatusView>(R.id.reviewStep1Status)

    private val reviewStep2DividerBottom = containerView.findViewById<View>(R.id.reviewStep2DividerBottom)
    private val reviewStep2Container = containerView.findViewById<ViewGroup>(R.id.reviewStep2Container)
    private val reviewStep2Loading = containerView.findViewById<View>(R.id.reviewStep2Loading)
    private val reviewStep2CreateSession = containerView.findViewById<View>(R.id.reviewStep2CreateSession)
    private val reviewStep2SelectSubmission = containerView.findViewById<View>(R.id.reviewStep2SelectSubmission)
    private val reviewStep2Retry = containerView.findViewById<View>(R.id.reviewStep2Retry)
    private val reviewStep2Title = containerView.findViewById<TextView>(R.id.reviewStep2Title)
    private val reviewStep2Link = containerView.findViewById<View>(R.id.reviewStep2Link)
    private val reviewStep2Status = containerView.findViewById<ReviewStatusView>(R.id.reviewStep2Status)

    private val reviewStep3Title = containerView.findViewById<TextView>(R.id.reviewStep3Title)
    private val reviewStep3Link = containerView.findViewById<View>(R.id.reviewStep3Link)
    private val reviewStep3Status = containerView.findViewById<ReviewStatusView>(R.id.reviewStep3Status)
    private val reviewStep3Container = containerView.findViewById<Button>(R.id.reviewStep3Container)
    private val reviewStep3Loading = containerView.findViewById<View>(R.id.reviewStep3Loading)

    private val reviewStep4Title = containerView.findViewById<TextView>(R.id.reviewStep4Title)
    private val reviewStep4Link = containerView.findViewById<View>(R.id.reviewStep4Link)
    private val reviewStep4Status = containerView.findViewById<ReviewStatusView>(R.id.reviewStep4Status)
    private val reviewStep4Container = containerView.findViewById<Button>(R.id.reviewStep4Container)
    private val reviewStep4Hint = containerView.findViewById<View>(R.id.reviewStep4Hint)

    private val reviewStep5Title = containerView.findViewById<TextView>(R.id.reviewStep5Title)
    private val reviewStep5Link = containerView.findViewById<View>(R.id.reviewStep5Link)
    private val reviewStep5Status = containerView.findViewById<ReviewStatusView>(R.id.reviewStep5Status)
    private val reviewStep5Container = containerView.findViewById<Button>(R.id.reviewStep5Container)
    private val reviewStep5Hint = containerView.findViewById<View>(R.id.reviewStep5Hint)

    private val stepQuizFeedbackMapper = StepQuizFeedbackMapper()

    private val step1viewStateDelegate = ViewStateDelegate<StepQuizReviewFeature.State>()
        .apply {
            addState<StepQuizReviewFeature.State.SubmissionNotMade>(
                reviewStep1DividerBottom, reviewStep1Container, reviewStep1Discounting,
                reviewStep1ActionButton, reviewStep1ActionRetry
            )
        }

    private val step1QuizViewStateDelegate = ViewStateDelegate<StepQuizFeature.State>()
        .apply {
            addState<StepQuizFeature.State.Loading>(stepQuizProgress)
            addState<StepQuizFeature.State.AttemptLoading>(stepQuizProgress)
            addState<StepQuizFeature.State.AttemptLoaded>(reviewStep1Discounting, reviewStep1QuizContainer, reviewStep1ActionButton, reviewStep1ActionRetry)
            addState<StepQuizFeature.State.NetworkError>(stepQuizNetworkError)
        }

    private val step2viewStateDelegate = ViewStateDelegate<StepQuizReviewFeature.State>()
        .apply {
            addState<StepQuizReviewFeature.State.SubmissionNotSelected>(
                reviewStep2DividerBottom, reviewStep2Container, reviewStep2Loading,
                reviewStep2CreateSession, reviewStep2SelectSubmission, reviewStep2Retry
            )
            addState<StepQuizReviewFeature.State.SubmissionSelected>(reviewStep2DividerBottom, reviewStep2Container)
            addState<StepQuizReviewFeature.State.Completed>(reviewStep2DividerBottom, reviewStep2Container)
        }

    init {
        stepQuizNetworkError.findViewById<View>(R.id.tryAgain).setOnClickListener { actionListener.onQuizTryAgainClicked() }

        reviewStep2SelectSubmission.setOnClickListener { actionListener.onSelectDifferentSubmissionClicked() }
        reviewStep2CreateSession.setOnClickListener { actionListener.onCreateSessionClicked() }
        reviewStep2Retry.setOnClickListener { actionListener.onSolveAgainClicked() }

        if (instructionType == ReviewStrategyType.PEER) {
            reviewStep3Container.setOnClickListener { actionListener.onStartReviewClicked() }
        }
    }

    fun render(state: StepQuizReviewFeature.State) {
        if (state is StepQuizReviewFeature.State.WithQuizState) {
            quizFeedbackBlocksDelegate.setState(stepQuizFeedbackMapper.mapToStepQuizFeedbackState(blockName, state.quizState))
        }

        renderStep1(state)
        renderStep2(state)

        if (instructionType == ReviewStrategyType.PEER) {
            renderStep3(state)
            renderStep4(state)
        }

        renderStep5(state)
    }

    private fun renderStep1(state: StepQuizReviewFeature.State) {
        step1viewStateDelegate.switchState(state)
        when (state) {
            is StepQuizReviewFeature.State.SubmissionNotMade -> {
                val submissionStatus = state.quizState.safeCast<StepQuizFeature.State.AttemptLoaded>()
                    ?.submissionState
                    ?.safeCast<StepQuizFeature.SubmissionState.Loaded>()
                    ?.submission
                    ?.status

                reviewStep1Status.status =
                    if (submissionStatus == Submission.Status.WRONG) {
                        ReviewStatusView.Status.ERROR
                    } else {
                        ReviewStatusView.Status.IN_PROGRESS
                    }

                stepQuizDescription.isEnabled = true

                step1QuizViewStateDelegate.switchState(state.quizState)
                if (state.quizState is StepQuizFeature.State.AttemptLoaded) {
                    quizDelegate.setState(state.quizState)
                }

                setQuizViewParent(quizView, reviewStep1QuizContainer)
                setQuizViewParent(quizFeedbackView, reviewStep1QuizContainer)
            }

            else -> {
                stepQuizDescription.isEnabled = false
                reviewStep1Status.status = ReviewStatusView.Status.COMPLETED
            }
        }
    }

    private fun renderStep2(state: StepQuizReviewFeature.State) {
        step2viewStateDelegate.switchState(state)
        when (state) {
            is StepQuizReviewFeature.State.SubmissionNotMade -> {
                reviewStep2Title.setText(R.string.step_quiz_review_send_pending)
                setStepStatus(reviewStep2Title, reviewStep2Link, reviewStep2Status, ReviewStatusView.Status.PENDING)
            }
            is StepQuizReviewFeature.State.SubmissionNotSelected -> {
                reviewStep2Title.setText(R.string.step_quiz_review_send_in_progress)
                setStepStatus(reviewStep2Title, reviewStep2Link, reviewStep2Status, ReviewStatusView.Status.IN_PROGRESS)

                quizDelegate.setState(state.quizState)

                reviewStep2Loading.isVisible = state.isSessionCreationInProgress
                reviewStep2CreateSession.isVisible = !state.isSessionCreationInProgress
                reviewStep2SelectSubmission.isVisible = !state.isSessionCreationInProgress
                reviewStep2Retry.isVisible = !state.isSessionCreationInProgress

                setQuizViewParent(quizView, reviewStep2Container)
                setQuizViewParent(quizFeedbackView, reviewStep2Container)
            }
            else -> {
                reviewStep2Title.setText(R.string.step_quiz_review_send_completed)
                setStepStatus(reviewStep2Title, reviewStep2Link, reviewStep2Status, ReviewStatusView.Status.COMPLETED)

                state.safeCast<StepQuizReviewFeature.State.WithQuizState>()
                    ?.quizState
                    ?.safeCast<StepQuizFeature.State.AttemptLoaded>()
                    ?.let(quizDelegate::setState)

                setQuizViewParent(quizView, reviewStep2Container)
                setQuizViewParent(quizFeedbackView, reviewStep2Container)
                quizFeedbackView.isVisible = false
            }
        }
    }

    private fun setQuizViewParent(view: View, parent: ViewGroup) {
        val currentParentViewGroup = view.parent.safeCast<ViewGroup>()
        if (currentParentViewGroup == parent) return

        currentParentViewGroup?.removeView(view)
        parent.addView(view)
    }

    private fun renderStep3(state: StepQuizReviewFeature.State) {
        val reviewCount = state.safeCast<StepQuizReviewFeature.State.WithInstruction>()?.instruction?.minReviews ?: 0

        when (state) {
            is StepQuizReviewFeature.State.SubmissionNotMade,
            is StepQuizReviewFeature.State.SubmissionNotSelected -> {
                reviewStep3Title.setText(R.string.step_quiz_review_given_pending_zero)
                setStepStatus(reviewStep3Title, reviewStep3Link, reviewStep3Status, ReviewStatusView.Status.PENDING)
                reviewStep3Container.isVisible = false
                reviewStep3Loading.isVisible = false
            }
            is StepQuizReviewFeature.State.SubmissionSelected -> {
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
            is StepQuizReviewFeature.State.Completed -> {
                val givenReviewCount = state.session.givenReviews.size

                reviewStep3Title.text = resources.getQuantityString(R.plurals.step_quiz_review_given_completed, givenReviewCount, givenReviewCount)
                reviewStep3Container.isVisible = false
                reviewStep3Loading.isVisible = false
                setStepStatus(reviewStep3Title, reviewStep3Link, reviewStep3Status, ReviewStatusView.Status.COMPLETED)
            }
            else -> Unit
        }
    }

    private fun renderStep4(state: StepQuizReviewFeature.State) {
        val reviewCount = state.safeCast<StepQuizReviewFeature.State.WithInstruction>()?.instruction?.minReviews ?: 0

        when (state) {
            is StepQuizReviewFeature.State.SubmissionNotMade,
            is StepQuizReviewFeature.State.SubmissionNotSelected -> {
                reviewStep4Title.setText(R.string.step_quiz_review_taken_pending_zero)
                setStepStatus(reviewStep4Title, reviewStep4Link, reviewStep4Status, ReviewStatusView.Status.PENDING)
                reviewStep4Container.isVisible = false
                reviewStep4Hint.isVisible = false
            }
            is StepQuizReviewFeature.State.SubmissionSelected -> {
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
                reviewStep4Hint.isVisible = takenReviewCount == 0
            }
            is StepQuizReviewFeature.State.Completed -> {
                val takenReviewCount = state.session.takenReviews.size
                reviewStep4Title.text = resources.getQuantityString(R.plurals.step_quiz_review_taken_completed, takenReviewCount, takenReviewCount)
                setStepStatus(reviewStep4Title, reviewStep4Link, reviewStep4Status, ReviewStatusView.Status.COMPLETED)
                reviewStep4Container.isVisible = takenReviewCount > 0
                reviewStep4Container.setOnClickListener { actionListener.onTakenReviewClicked(state.session.id) }
                reviewStep4Hint.isVisible = false
            }
            else -> Unit
        }
    }

    private fun renderStep5(state: StepQuizReviewFeature.State) {
        reviewStep5Status.position =
            when (instructionType) {
                ReviewStrategyType.PEER -> 5
                ReviewStrategyType.INSTRUCTOR -> 3
            }

        when (state) {
            is StepQuizReviewFeature.State.Completed -> {
                val receivedPoints = state.progress?.score?.toFloatOrNull() ?: 0f

                reviewStep5Title.text = ProgressTextMapper
                    .mapProgressToText(
                        reviewStep5Title.context,
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
                reviewStep5Hint.isVisible = false
            }
            else -> {
                val cost = state.safeCast<StepQuizReviewFeature.State.WithProgress>()?.progress?.cost ?: 0L

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
                    if (state is StepQuizReviewFeature.State.SubmissionSelected && instructionType == ReviewStrategyType.INSTRUCTOR) {
                        ReviewStatusView.Status.IN_PROGRESS
                    } else {
                        ReviewStatusView.Status.PENDING
                    }

                reviewStep5Hint.isVisible = instructionType == ReviewStrategyType.INSTRUCTOR && status == ReviewStatusView.Status.IN_PROGRESS

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
        fun onCreateSessionClicked()
        fun onSolveAgainClicked()

        fun onQuizTryAgainClicked()

        fun onStartReviewClicked()
        fun onTakenReviewClicked(sessionId: Long)
    }
}
