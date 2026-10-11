package org.stepik.android.view.step_quiz_review.ui.delegate

import android.view.View
import android.view.ViewGroup
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import org.stepic.droid.R
import org.stepic.droid.databinding.FragmentStepQuizReviewPeerBinding
import org.stepic.droid.databinding.LayoutStepQuizReviewFooterBinding
import org.stepic.droid.databinding.LayoutStepQuizReviewHeaderBinding
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
    private val headerBinding: LayoutStepQuizReviewHeaderBinding,
    private val footerBinding: LayoutStepQuizReviewFooterBinding,
    private val reviewStep5Link: View,
    private val peerBinding: FragmentStepQuizReviewPeerBinding?,
    private val instructionType: ReviewStrategyType,
    private val actionListener: ActionListener,

    private val blockName: String?,
    private val quizView: View,
    private val quizDelegate: StepQuizDelegate,
    private val quizFeedbackBlocksDelegate: StepQuizFeedbackBlocksDelegate
) {
    private val stepQuizFeedbackMapper = StepQuizFeedbackMapper()

    private val step1viewStateDelegate = ViewStateDelegate<StepQuizReviewFeature.State>()
        .apply {
            addState<StepQuizReviewFeature.State.SubmissionNotMade>(
                headerBinding.reviewStep1DividerBottom.root,
                headerBinding.reviewStep1Container,
                headerBinding.reviewStep1Discounting,
                headerBinding.reviewStep1ActionButton,
                headerBinding.reviewStep1ActionRetry
            )
        }

    private val step1QuizViewStateDelegate = ViewStateDelegate<StepQuizFeature.State>()
        .apply {
            addState<StepQuizFeature.State.Loading>(headerBinding.stepQuizProgress)
            addState<StepQuizFeature.State.AttemptLoading>(headerBinding.stepQuizProgress)
            addState<StepQuizFeature.State.AttemptLoaded>(
                headerBinding.reviewStep1Discounting,
                headerBinding.reviewStep1QuizContainer,
                headerBinding.reviewStep1ActionButton,
                headerBinding.reviewStep1ActionRetry
            )
            addState<StepQuizFeature.State.NetworkError>(headerBinding.stepQuizNetworkError.root)
        }

    private val step2viewStateDelegate = ViewStateDelegate<StepQuizReviewFeature.State>()
        .apply {
            addState<StepQuizReviewFeature.State.SubmissionNotSelected>(
                headerBinding.reviewStep2DividerBottom.root,
                headerBinding.reviewStep2Container,
                headerBinding.reviewStep2Loading,
                headerBinding.reviewStep2CreateSession,
                headerBinding.reviewStep2SelectSubmission,
                headerBinding.reviewStep2Retry
            )
            addState<StepQuizReviewFeature.State.SubmissionSelected>(
                headerBinding.reviewStep2DividerBottom.root,
                headerBinding.reviewStep2Container
            )
            addState<StepQuizReviewFeature.State.Completed>(
                headerBinding.reviewStep2DividerBottom.root,
                headerBinding.reviewStep2Container
            )
        }

    init {
        headerBinding.stepQuizNetworkError.tryAgain.setOnClickListener { actionListener.onQuizTryAgainClicked() }

        headerBinding.reviewStep2SelectSubmission.setOnClickListener { actionListener.onSelectDifferentSubmissionClicked() }
        headerBinding.reviewStep2CreateSession.setOnClickListener { actionListener.onCreateSessionClicked() }
        headerBinding.reviewStep2Retry.setOnClickListener { actionListener.onSolveAgainClicked() }

        if (instructionType == ReviewStrategyType.PEER) {
            requireNotNull(peerBinding) { "Peer review binding is required for peer review steps" }
                .reviewStep3Container
                .setOnClickListener { actionListener.onStartReviewClicked() }
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

                headerBinding.reviewStep1Status.status =
                    if (submissionStatus == Submission.Status.WRONG) {
                        ReviewStatusView.Status.ERROR
                    } else {
                        ReviewStatusView.Status.IN_PROGRESS
                    }

                headerBinding.stepQuizDescription.isEnabled = true

                step1QuizViewStateDelegate.switchState(state.quizState)
                if (state.quizState is StepQuizFeature.State.AttemptLoaded) {
                    quizDelegate.setState(state.quizState)
                }

                setQuizViewParent(quizView, headerBinding.reviewStep1QuizContainer)
                setQuizViewParent(headerBinding.quizFeedbackView.root, headerBinding.reviewStep1QuizContainer)
            }

            else -> {
                headerBinding.stepQuizDescription.isEnabled = false
                headerBinding.reviewStep1Status.status = ReviewStatusView.Status.COMPLETED
            }
        }
    }

    private fun renderStep2(state: StepQuizReviewFeature.State) {
        step2viewStateDelegate.switchState(state)
        when (state) {
            is StepQuizReviewFeature.State.SubmissionNotMade -> {
                headerBinding.reviewStep2Title.setText(R.string.step_quiz_review_send_pending)
                setStepStatus(
                    headerBinding.reviewStep2Title,
                    headerBinding.reviewStep2Link,
                    headerBinding.reviewStep2Status,
                    ReviewStatusView.Status.PENDING
                )
            }
            is StepQuizReviewFeature.State.SubmissionNotSelected -> {
                headerBinding.reviewStep2Title.setText(R.string.step_quiz_review_send_in_progress)
                setStepStatus(
                    headerBinding.reviewStep2Title,
                    headerBinding.reviewStep2Link,
                    headerBinding.reviewStep2Status,
                    ReviewStatusView.Status.IN_PROGRESS
                )

                quizDelegate.setState(state.quizState)

                headerBinding.reviewStep2Loading.isVisible = state.isSessionCreationInProgress
                headerBinding.reviewStep2CreateSession.isVisible = !state.isSessionCreationInProgress
                headerBinding.reviewStep2SelectSubmission.isVisible = !state.isSessionCreationInProgress
                headerBinding.reviewStep2Retry.isVisible = !state.isSessionCreationInProgress

                setQuizViewParent(quizView, headerBinding.reviewStep2Container)
                setQuizViewParent(headerBinding.quizFeedbackView.root, headerBinding.reviewStep2Container)
            }
            else -> {
                headerBinding.reviewStep2Title.setText(R.string.step_quiz_review_send_completed)
                setStepStatus(
                    headerBinding.reviewStep2Title,
                    headerBinding.reviewStep2Link,
                    headerBinding.reviewStep2Status,
                    ReviewStatusView.Status.COMPLETED
                )

                state.safeCast<StepQuizReviewFeature.State.WithQuizState>()
                    ?.quizState
                    ?.safeCast<StepQuizFeature.State.AttemptLoaded>()
                    ?.let(quizDelegate::setState)

                setQuizViewParent(quizView, headerBinding.reviewStep2Container)
                setQuizViewParent(headerBinding.quizFeedbackView.root, headerBinding.reviewStep2Container)
                headerBinding.quizFeedbackView.root.isVisible = false
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
        val requiredPeerBinding = requireNotNull(peerBinding) { "Peer review binding is required for peer review steps" }
        val reviewCount = state.safeCast<StepQuizReviewFeature.State.WithInstruction>()?.instruction?.minReviews ?: 0

        when (state) {
            is StepQuizReviewFeature.State.SubmissionNotMade,
            is StepQuizReviewFeature.State.SubmissionNotSelected -> {
                requiredPeerBinding.reviewStep3Title.setText(R.string.step_quiz_review_given_pending_zero)
                setStepStatus(
                    requiredPeerBinding.reviewStep3Title,
                    requiredPeerBinding.reviewStep3Link,
                    requiredPeerBinding.reviewStep3Status,
                    ReviewStatusView.Status.PENDING
                )
                requiredPeerBinding.reviewStep3Container.isVisible = false
                requiredPeerBinding.reviewStep3Loading.isVisible = false
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
                            append(headerBinding.root.resources.getQuantityString(pluralRes, remainingReviewCount, remainingReviewCount))
                        }

                        if (givenReviewCount > 0) {
                            if (isNotEmpty()) {
                                append(" ")
                            }
                            append(headerBinding.root.resources.getQuantityString(R.plurals.step_quiz_review_given_completed, givenReviewCount, givenReviewCount))
                        }
                    }

                requiredPeerBinding.reviewStep3Title.text = text

                requiredPeerBinding.reviewStep3Container.isVisible = remainingReviewCount > 0 && !state.isReviewCreationInProgress

                if (requiredPeerBinding.reviewStep3Container.isVisible) {
                    requiredPeerBinding.reviewStep3Container.isEnabled = remainingReviewCount <= 0 || state.session.isReviewAvailable
                    requiredPeerBinding.reviewStep3Container.setText(
                        if (requiredPeerBinding.reviewStep3Container.isEnabled) {
                            R.string.step_quiz_review_given_start_review
                        } else {
                            R.string.step_quiz_review_given_no_review
                        }
                    )
                }

                requiredPeerBinding.reviewStep3Loading.isVisible = state.isReviewCreationInProgress
                setStepStatus(
                    requiredPeerBinding.reviewStep3Title,
                    requiredPeerBinding.reviewStep3Link,
                    requiredPeerBinding.reviewStep3Status,
                    ReviewStatusView.Status.IN_PROGRESS
                )
            }
            is StepQuizReviewFeature.State.Completed -> {
                val givenReviewCount = state.session.givenReviews.size

                requiredPeerBinding.reviewStep3Title.text =
                    headerBinding.root.resources.getQuantityString(R.plurals.step_quiz_review_given_completed, givenReviewCount, givenReviewCount)
                requiredPeerBinding.reviewStep3Container.isVisible = false
                requiredPeerBinding.reviewStep3Loading.isVisible = false
                setStepStatus(
                    requiredPeerBinding.reviewStep3Title,
                    requiredPeerBinding.reviewStep3Link,
                    requiredPeerBinding.reviewStep3Status,
                    ReviewStatusView.Status.COMPLETED
                )
            }
            else -> Unit
        }
    }

    private fun renderStep4(state: StepQuizReviewFeature.State) {
        val requiredPeerBinding = requireNotNull(peerBinding) { "Peer review binding is required for peer review steps" }
        val reviewCount = state.safeCast<StepQuizReviewFeature.State.WithInstruction>()?.instruction?.minReviews ?: 0

        when (state) {
            is StepQuizReviewFeature.State.SubmissionNotMade,
            is StepQuizReviewFeature.State.SubmissionNotSelected -> {
                requiredPeerBinding.reviewStep4Title.setText(R.string.step_quiz_review_taken_pending_zero)
                setStepStatus(
                    requiredPeerBinding.reviewStep4Title,
                    requiredPeerBinding.reviewStep4Link,
                    requiredPeerBinding.reviewStep4Status,
                    ReviewStatusView.Status.PENDING
                )
                requiredPeerBinding.reviewStep4Container.isVisible = false
                requiredPeerBinding.reviewStep4Hint.isVisible = false
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
                            append(headerBinding.root.resources.getQuantityString(pluralRes, remainingReviewCount, remainingReviewCount))
                        }

                        if (takenReviewCount > 0) {
                            if (isNotEmpty()) {
                                append(" ")
                            }
                            append(headerBinding.root.resources.getQuantityString(R.plurals.step_quiz_review_taken_completed, takenReviewCount, takenReviewCount))
                        }
                    }

                val status =
                    if (remainingReviewCount > 0) {
                        ReviewStatusView.Status.IN_PROGRESS
                    } else {
                        ReviewStatusView.Status.COMPLETED
                    }

                requiredPeerBinding.reviewStep4Title.text = text
                setStepStatus(
                    requiredPeerBinding.reviewStep4Title,
                    requiredPeerBinding.reviewStep4Link,
                    requiredPeerBinding.reviewStep4Status,
                    status
                )

                requiredPeerBinding.reviewStep4Container.isVisible = takenReviewCount > 0
                requiredPeerBinding.reviewStep4Container.setOnClickListener { actionListener.onTakenReviewClicked(state.session.id) }
                requiredPeerBinding.reviewStep4Hint.isVisible = takenReviewCount == 0
            }
            is StepQuizReviewFeature.State.Completed -> {
                val takenReviewCount = state.session.takenReviews.size
                requiredPeerBinding.reviewStep4Title.text =
                    headerBinding.root.resources.getQuantityString(R.plurals.step_quiz_review_taken_completed, takenReviewCount, takenReviewCount)
                setStepStatus(
                    requiredPeerBinding.reviewStep4Title,
                    requiredPeerBinding.reviewStep4Link,
                    requiredPeerBinding.reviewStep4Status,
                    ReviewStatusView.Status.COMPLETED
                )
                requiredPeerBinding.reviewStep4Container.isVisible = takenReviewCount > 0
                requiredPeerBinding.reviewStep4Container.setOnClickListener { actionListener.onTakenReviewClicked(state.session.id) }
                requiredPeerBinding.reviewStep4Hint.isVisible = false
            }
            else -> Unit
        }
    }

    private fun renderStep5(state: StepQuizReviewFeature.State) {
        footerBinding.reviewStep5Status.position =
            when (instructionType) {
                ReviewStrategyType.PEER -> 5
                ReviewStrategyType.INSTRUCTOR -> 3
            }

        when (state) {
            is StepQuizReviewFeature.State.Completed -> {
                val receivedPoints = state.progress?.score?.toFloatOrNull() ?: 0f

                footerBinding.reviewStep5Title.text = ProgressTextMapper
                    .mapProgressToText(
                        footerBinding.reviewStep5Title.context,
                        receivedPoints,
                        state.progress?.cost ?: 0,
                        R.string.step_quiz_review_peer_completed,
                        R.string.step_quiz_review_peer_completed,
                        R.plurals.points
                    )

                when (instructionType) {
                    ReviewStrategyType.PEER ->
                        footerBinding.reviewStep5Container.isVisible = false

                    ReviewStrategyType.INSTRUCTOR -> {
                        footerBinding.reviewStep5Container.setOnClickListener { actionListener.onTakenReviewClicked(state.session.id) }
                        footerBinding.reviewStep5Container.isVisible = true
                    }
                }
                setStepStatus(
                    footerBinding.reviewStep5Title,
                    reviewStep5Link,
                    footerBinding.reviewStep5Status,
                    ReviewStatusView.Status.IN_PROGRESS
                )
                footerBinding.reviewStep5Status.status = ReviewStatusView.Status.COMPLETED
                footerBinding.reviewStep5Hint.isVisible = false
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

                footerBinding.reviewStep5Title.text =
                    headerBinding.root.resources.getString(
                        stringRes,
                        headerBinding.root.resources.getQuantityString(R.plurals.points, cost.toInt(), cost)
                    )
                footerBinding.reviewStep5Container.isVisible = false
                val status =
                    if (state is StepQuizReviewFeature.State.SubmissionSelected && instructionType == ReviewStrategyType.INSTRUCTOR) {
                        ReviewStatusView.Status.IN_PROGRESS
                    } else {
                        ReviewStatusView.Status.PENDING
                    }

                footerBinding.reviewStep5Hint.isVisible =
                    instructionType == ReviewStrategyType.INSTRUCTOR && status == ReviewStatusView.Status.IN_PROGRESS

                setStepStatus(
                    footerBinding.reviewStep5Title,
                    reviewStep5Link,
                    footerBinding.reviewStep5Status,
                    status
                )
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
