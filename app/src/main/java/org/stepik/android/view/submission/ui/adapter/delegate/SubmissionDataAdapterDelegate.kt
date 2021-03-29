package org.stepik.android.view.submission.ui.adapter.delegate

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import kotlinx.android.synthetic.main.item_submission_data.view.*
import kotlinx.android.synthetic.main.view_submission_review.view.*
import org.stepic.droid.R
import org.stepik.android.view.glide.ui.extension.wrapWithGlide
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.resolveFloatAttribute
import org.stepic.droid.util.toFixed
import org.stepik.android.domain.review_instruction.model.ReviewInstruction
import org.stepik.android.domain.submission.model.SubmissionItem
import org.stepik.android.model.Submission
import org.stepik.android.model.user.User
import org.stepik.android.view.base.ui.mapper.DateMapper
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import kotlin.math.roundToInt

class SubmissionDataAdapterDelegate(
    private val currentUserId: Long,
    private val isTeacher: Boolean,
    private val isSelectionEnabled: Boolean,
    private val reviewInstruction: ReviewInstruction?,
    private val actionListener: ActionListener
) : AdapterDelegate<SubmissionItem, DelegateViewHolder<SubmissionItem>>() {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<SubmissionItem> =
        ViewHolder(createView(parent, R.layout.item_submission_data))

    override fun isForViewType(position: Int, data: SubmissionItem): Boolean =
        data is SubmissionItem.Data

    private inner class ViewHolder(root: View) : DelegateViewHolder<SubmissionItem>(root), View.OnClickListener {
        private val submissionContainer = root.submissionContainer
        private val submissionUserIcon = root.submissionUserIcon
        private val submissionUserIconWrapper = submissionUserIcon.wrapWithGlide()
        private val submissionUserName = root.submissionUserName

        private val submissionTime = root.submissionTime
        private val submissionSolution = root.submissionSolution
        private val submissionMoreIcon = root.submissionMoreIcon
        private val submissionSelect = root.submissionSelect
        private val submissionStatus = root.submissionStatus
        private val submissionScoreValue = root.submissionScoreValue
        private val submissionScoreText = root.submissionScoreText
        private val reviewSelect = root.reviewSelect
        private val reviewSelectText = root.reviewSelectText
        private val reviewSelectArrow = root.reviewSelectArrow

        init {
            submissionContainer.setOnClickListener(this)
            submissionUserIcon.setOnClickListener(this)
            submissionUserName.setOnClickListener(this)
            submissionMoreIcon.setOnClickListener(this)
            reviewSelect.setOnClickListener(this)

            if (isSelectionEnabled) {
                submissionSelect.setOnClickListener(this)
            }

            root.submissionDivider.isVisible = isSelectionEnabled || reviewInstruction != null
            reviewSelect.isVisible = reviewInstruction != null
            submissionSelect.isVisible = isSelectionEnabled
            submissionMoreIcon.isVisible = isTeacher
        }
        override fun onBind(data: SubmissionItem) {
            data as SubmissionItem.Data

            submissionUserName.text = data.user.fullName
            submissionUserIconWrapper.setImagePath(data.user.avatar ?: "", AppCompatResources.getDrawable(context, R.drawable.general_placeholder))
            submissionTime.text = DateMapper.mapToRelativeDate(context, DateTimeHelper.nowUtc(), data.submission.time?.time ?: 0)

            setupSubmission(data.submission)
            setupReviewView(data, getSubmissionReviewState(data))
        }

        override fun onClick(view: View) {
            val dataItem = itemData as? SubmissionItem.Data
                ?: return

            when (view.id) {
                R.id.submissionUserIcon,
                R.id.submissionUserName ->
                    actionListener.onUserClicked(dataItem.user)

                R.id.submissionMoreIcon ->
                    showItemMenu(view)

                R.id.submissionSelect ->
                    actionListener.onItemClicked(dataItem)

                R.id.reviewSelect -> {
                    val reviewState = getSubmissionReviewState(dataItem) ?: return
                    if (reviewState == ReviewState.NOT_SUBMITTED_FOR_REVIEW) {
                        actionListener.onSeeSubmissionReviewAction(dataItem.submission.id)
                    } else {
                        dataItem.reviewSessionData?.let { actionListener.onSeeReviewsReviewAction(it.id) }
                    }
                }

                R.id.submissionContainer ->
                    actionListener.onSubmissionClicked(dataItem)
            }
        }

        private fun showItemMenu(view: View) {
            val submissionItemData = itemData as? SubmissionItem.Data
                ?: return

            val popupMenu = PopupMenu(context, view)
            popupMenu.inflate(R.menu.submission_item_menu)

            popupMenu
                .setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.submission_item_submissions ->
                            actionListener.onViewSubmissionsClicked(submissionItemData)
                    }
                    true
                }

            popupMenu.show()
        }

        private fun setupSubmission(submission: Submission) {
            val (tintColor, statusText) =
                when (submission.status) {
                    Submission.Status.CORRECT ->
                        ContextCompat.getColor(context, R.color.color_overlay_green) to
                                context.resources.getString(R.string.submission_status_correct)

                    Submission.Status.PARTIALLY_CORRECT ->
                        ContextCompat.getColor(context, R.color.color_overlay_yellow) to
                                context.resources.getString(R.string.submission_status_partially_correct)

                    Submission.Status.WRONG ->
                        ContextCompat.getColor(context, R.color.color_red_300) to
                                context.resources.getString(R.string.submission_status_incorrect)

                    else ->
                        R.color.transparent to ""
                }

            submissionStatus.setTextColor(tintColor)
            submissionStatus.text = statusText
            submissionScoreValue.text = getSubmissionValue(submission)
            submissionSolution.text = context.getString(R.string.comment_solution_number, submission.id)
            TextViewCompat.setCompoundDrawableTintList(submissionSolution, ColorStateList.valueOf(tintColor))

            val needShowScore = (submission.score?.toFloatOrNull() ?: 0f) > 0f
            submissionScoreValue.isVisible = needShowScore
            submissionScoreText.isVisible = needShowScore
        }

        private fun getSubmissionValue(submission: Submission): String {
            val submissionScore = submission.score?.toFloatOrNull() ?: 0f
            return if (submissionScore < 1f) {
                submissionScore.toFixed(2)
            } else {
                submissionScore.roundToInt().toString()
            }
        }

        private fun setupReviewView(submissionItemData: SubmissionItem.Data, reviewState: ReviewState?) {
            if (reviewState == null) return

            val title = when (reviewState) {
                ReviewState.IN_PROGRESS, ReviewState.FINISHED -> {
                    val takenReviewCount = submissionItemData.reviewSessionData?.session?.takenReviews?.size ?: 0
                    val minReviewsCount = reviewInstruction?.minReviews ?: 0
                    context.getString(R.string.submission_review_state_in_progress_title, takenReviewCount, minReviewsCount)
                }

                ReviewState.CANT_REVIEW_WRONG, ReviewState.CANT_REVIEW_ANOTHER, ReviewState.CANT_REVIEW_TEACHER ->
                    context.getString(R.string.submission_review_state_cannot_review_title)

                ReviewState.NOT_SUBMITTED_FOR_REVIEW ->
                    context.getString(R.string.submission_review_state_not_submitted_title)
            }

            val message = context.getString(reviewState.messageResId)

            val isEnabled = when (reviewState) {
                ReviewState.IN_PROGRESS, ReviewState.FINISHED, ReviewState.NOT_SUBMITTED_FOR_REVIEW ->
                    true
                ReviewState.CANT_REVIEW_WRONG, ReviewState.CANT_REVIEW_ANOTHER, ReviewState.CANT_REVIEW_TEACHER ->
                    false
            }

            val actionTitle = if (reviewState == ReviewState.NOT_SUBMITTED_FOR_REVIEW) {
                context.getString(R.string.submission_review_action_see_submissions_title)
            } else {
                context.getString(R.string.submission_review_action_see_reviews_title)
            }

            reviewSelectText.text = buildSpannedString {
                append("$title\n")
                append("$message\n")
                color(ContextCompat.getColor(context, R.color.color_overlay_violet)) {
                    append(actionTitle)
                }
            }
            reviewSelect.isEnabled = isEnabled
            val alpha = if (isEnabled) 1f else context.resolveFloatAttribute(R.attr.alphaEmphasisDisabled)
            reviewSelectText.alpha = alpha
            reviewSelectArrow.alpha = alpha
        }

        private fun getSubmissionReviewState(itemData: SubmissionItem.Data): ReviewState? {
            if (reviewInstruction == null) {
                return null
            }

            if (itemData.submission.status == Submission.Status.EVALUATION) {
                return null
            }

            itemData.submission.session?.let {
                itemData.reviewSessionData?.let { reviewSessionData ->
                    return if (reviewSessionData.session.isFinished) {
                        ReviewState.FINISHED
                    } else {
                        ReviewState.IN_PROGRESS
                    }
                }
            }

            if (itemData.submission.status == Submission.Status.WRONG) {
                return ReviewState.CANT_REVIEW_WRONG
            }

            if (itemData.attempt.user == currentUserId && isTeacher) {
                return ReviewState.CANT_REVIEW_TEACHER
            }

            if (itemData.submission.session == null && itemData.reviewSessionData == null) {
                return ReviewState.NOT_SUBMITTED_FOR_REVIEW
            }

            return ReviewState.CANT_REVIEW_ANOTHER
        }
    }

    enum class ReviewState(@StringRes val messageResId: Int) {
        IN_PROGRESS(R.string.submission_review_state_in_progress_message),
        FINISHED(R.string.submission_review_state_finished_message),
        CANT_REVIEW_WRONG(R.string.submission_review_state_cannot_review_wrong_message),
        CANT_REVIEW_TEACHER(R.string.submission_review_state_cannot_review_teacher_message),
        CANT_REVIEW_ANOTHER(R.string.submission_review_state_cannot_review_another_message),
        NOT_SUBMITTED_FOR_REVIEW(R.string.submission_review_state_not_submitted_message)
    }

    interface ActionListener {
        fun onUserClicked(user: User)
        fun onSubmissionClicked(data: SubmissionItem.Data)
        fun onItemClicked(data: SubmissionItem.Data)

        fun onViewSubmissionsClicked(submissionDataItem: SubmissionItem.Data)
        fun onSeeSubmissionReviewAction(submissionId: Long)
        fun onSeeReviewsReviewAction(session: Long)
    }
}