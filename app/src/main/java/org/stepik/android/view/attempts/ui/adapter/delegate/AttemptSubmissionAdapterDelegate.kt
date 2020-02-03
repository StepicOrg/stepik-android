package org.stepik.android.view.attempts.ui.adapter.delegate

import android.graphics.PorterDuff
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.item_attempt_submission.view.*
import org.stepic.droid.R
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.model.Submission
import org.stepik.android.view.attempts.model.AttemptCacheItem
import org.stepik.android.view.base.ui.mapper.DateMapper
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.selection.SelectionHelper

class AttemptSubmissionAdapterDelegate(
    private val selectionHelper: SelectionHelper,
    private val onCheckboxClick: (AttemptCacheItem.SubmissionItem) -> Unit,
    private val onItemClick: (AttemptCacheItem.SubmissionItem) -> Unit
) : AdapterDelegate<AttemptCacheItem, DelegateViewHolder<AttemptCacheItem>>() {
    override fun isForViewType(position: Int, data: AttemptCacheItem): Boolean =
        data is AttemptCacheItem.SubmissionItem

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<AttemptCacheItem> =
        ViewHolder(createView(parent, R.layout.item_attempt_submission))

    private inner class ViewHolder(root: View) : DelegateViewHolder<AttemptCacheItem>(root) {

        private val submissionRoot = root
        private val submissionQuizIcon = root.submissionQuizIcon
        private val submissionTitle = root.submissionTitle
        private val submissionStep = root.submissionStep
        private val submissionCheckBox = root.submissionCheckBox
        private val submissionStatusIconWrong = root.submissionStatusIconWrong
        private val submissionStatusIconCorrect = root.submissionStatusIconCorrect

        init {
            root.setOnClickListener { onItemClick(itemData as AttemptCacheItem.SubmissionItem) }
            submissionCheckBox.setOnClickListener { onCheckboxClick(itemData as AttemptCacheItem.SubmissionItem) }
        }

        override fun onBind(data: AttemptCacheItem) {
            data as AttemptCacheItem.SubmissionItem

            selectionHelper.isSelected(adapterPosition).let { isSelected ->
                itemView.isSelected = isSelected
                submissionCheckBox.isChecked = isSelected
            }

            submissionCheckBox.isEnabled = data.isEnabled

            val resourceId = when (data.step.block?.name) {
                AppConstants.TYPE_CODE ->
                    R.drawable.ic_hard_quiz
                else ->
                    R.drawable.ic_easy_quiz
            }

            val icon = AppCompatResources
                .getDrawable(context, resourceId)
                ?.mutate()
            icon?.setColorFilter(
                ContextCompat.getColor(context, R.color.new_accent_color), PorterDuff.Mode.SRC_IN)
            submissionQuizIcon.setImageDrawable(icon)
            submissionTitle.text =  HtmlCompat.fromHtml(data.step.block?.text ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            submissionStep.text = context.resources.getString(R.string.attempts_submission_step_position, data.step.position, DateMapper.mapToRelativeDate(context, DateTimeHelper.nowUtc(), data.time.time))

            when (data.submission.status) {
                Submission.Status.CORRECT -> {
                    submissionRoot.setBackgroundResource(R.drawable.bg_attempt_submission_correct_item)
                    submissionStatusIconCorrect.isVisible = true
                    submissionStatusIconWrong.isVisible = false
                    submissionCheckBox.visibility = View.INVISIBLE
                }
                Submission.Status.WRONG -> {
                    submissionRoot.setBackgroundResource(R.drawable.bg_attempt_submission_incorrect_item)
                    submissionStatusIconCorrect.isVisible = false
                    submissionStatusIconWrong.isVisible = true
                    submissionCheckBox.visibility = View.INVISIBLE
                }
                else -> {
                    submissionRoot.setBackgroundResource(R.drawable.bg_attempt_submission_item)
                    submissionStatusIconCorrect.isVisible = false
                    submissionStatusIconWrong.isVisible = false
                    submissionCheckBox.visibility = View.VISIBLE
                }
            }
        }
    }
}