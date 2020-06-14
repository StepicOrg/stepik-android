package org.stepik.android.view.submission.ui.adapter.delegate

import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.item_submission_data.view.*
import org.stepic.droid.R
import org.stepik.android.view.glide.ui.extension.wrapWithGlide
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.submission.model.SubmissionItem
import org.stepik.android.model.user.User
import org.stepik.android.view.base.ui.mapper.DateMapper
import org.stepik.android.view.submission.ui.delegate.setSubmission
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class SubmissionDataAdapterDelegate(
    private val isItemClickEnabled: Boolean,
    private val actionListener: ActionListener
) : AdapterDelegate<SubmissionItem, DelegateViewHolder<SubmissionItem>>() {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<SubmissionItem> =
        ViewHolder(createView(parent, R.layout.item_submission_data))

    override fun isForViewType(position: Int, data: SubmissionItem): Boolean =
        data is SubmissionItem.Data

    private inner class ViewHolder(root: View) : DelegateViewHolder<SubmissionItem>(root), View.OnClickListener {
        private val submissionUserIcon = root.submissionUserIcon
        private val submissionUserIconWrapper = submissionUserIcon.wrapWithGlide()
        private val submissionUserName = root.submissionUserName

        private val submissionTime = root.submissionTime
        private val submissionSolution = root.submissionSolution

        private val submissionUserIconPlaceholder = with(context.resources) {
            val coursePlaceholderBitmap = BitmapFactory.decodeResource(this, R.drawable.general_placeholder)
            val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(this, coursePlaceholderBitmap)
            circularBitmapDrawable.cornerRadius = getDimension(R.dimen.course_image_radius)
            circularBitmapDrawable
        }

        init {
            submissionSolution.setOnClickListener(this)

            submissionUserIcon.setOnClickListener(this)
            submissionUserName.setOnClickListener(this)

            if (isItemClickEnabled) {
                itemView.setOnClickListener(this)
            }

            root.submissionSelect.isVisible = isItemClickEnabled
        }
        override fun onBind(data: SubmissionItem) {
            data as SubmissionItem.Data

            submissionUserName.text = data.user.fullName
            submissionUserIconWrapper.setImagePath(data.user.avatar ?: "", submissionUserIconPlaceholder)

            submissionTime.text = DateMapper.mapToRelativeDate(context, DateTimeHelper.nowUtc(), data.submission.time?.time ?: 0)

            submissionSolution.setSubmission(data.submission)
        }

        override fun onClick(view: View) {
            val dataItem = itemData as? SubmissionItem.Data
                ?: return

            when (view.id) {
                R.id.submissionUserIcon,
                R.id.submissionUserName ->
                    actionListener.onUserClicked(dataItem.user)

                R.id.submissionSolution ->
                    actionListener.onSubmissionClicked(dataItem)

                itemView.id ->
                    actionListener.onItemClicked(dataItem)
            }
        }
    }

    interface ActionListener {
        fun onUserClicked(user: User)
        fun onSubmissionClicked(data: SubmissionItem.Data)
        fun onItemClicked(data: SubmissionItem.Data)
    }
}