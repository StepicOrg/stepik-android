package org.stepik.android.view.submission.ui.adapter.delegate

import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import kotlinx.android.synthetic.main.item_submission_data.view.*
import org.stepic.droid.R
import org.stepik.android.view.glide.ui.extension.wrapWithGlide
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.submission.model.SubmissionItem
import org.stepik.android.model.Submission
import org.stepik.android.model.user.User
import org.stepik.android.view.base.ui.mapper.DateMapper
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class SubmissionDataAdapterDelegate(
    private val isTeacher: Boolean,
    private val isSelectionEnabled: Boolean,
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

        private val submissionUserIconPlaceholder = with(context.resources) {
            val coursePlaceholderBitmap = BitmapFactory.decodeResource(this, R.drawable.general_placeholder)
            val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(this, coursePlaceholderBitmap)
            circularBitmapDrawable.cornerRadius = getDimension(R.dimen.course_image_radius)
            circularBitmapDrawable
        }

        init {
            submissionContainer.setOnClickListener(this)
            submissionUserIcon.setOnClickListener(this)
            submissionUserName.setOnClickListener(this)
            submissionMoreIcon.setOnClickListener(this)

            if (isSelectionEnabled) {
                submissionSelect.setOnClickListener(this)
            }

            root.submissionDivider.isVisible = isSelectionEnabled
            submissionSelect.isVisible = isSelectionEnabled
            submissionMoreIcon.isVisible = isTeacher
        }
        override fun onBind(data: SubmissionItem) {
            data as SubmissionItem.Data

            submissionUserName.text = data.user.fullName
            submissionUserIconWrapper.setImagePath(data.user.avatar ?: "", submissionUserIconPlaceholder)
            submissionTime.text = DateMapper.mapToRelativeDate(context, DateTimeHelper.nowUtc(), data.submission.time?.time ?: 0)

            setupSubmission(data.submission)
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
            submissionScoreValue.text = submission.score
            submissionSolution.text = context.getString(R.string.comment_solution_number, submission.id)
            TextViewCompat.setCompoundDrawableTintList(submissionSolution, ColorStateList.valueOf(tintColor))
        }
    }

    interface ActionListener {
        fun onUserClicked(user: User)
        fun onSubmissionClicked(data: SubmissionItem.Data)
        fun onItemClicked(data: SubmissionItem.Data)

        fun onViewSubmissionsClicked(submissionDataItem: SubmissionItem.Data)
    }
}