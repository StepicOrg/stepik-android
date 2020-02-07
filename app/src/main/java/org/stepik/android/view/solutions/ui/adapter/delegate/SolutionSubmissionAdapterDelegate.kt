package org.stepik.android.view.solutions.ui.adapter.delegate

import android.graphics.PorterDuff
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import kotlinx.android.synthetic.main.item_solution_submission.view.*
import org.stepic.droid.R
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.solutions.model.SolutionItem
import org.stepik.android.model.Submission
import org.stepik.android.view.base.ui.mapper.DateMapper
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.selection.SelectionHelper

class SolutionSubmissionAdapterDelegate(
    private val selectionHelper: SelectionHelper,
    private val onCheckboxClick: (SolutionItem.SubmissionItem) -> Unit,
    private val onItemClick: (SolutionItem.SubmissionItem) -> Unit
) : AdapterDelegate<SolutionItem, DelegateViewHolder<SolutionItem>>() {
    override fun isForViewType(position: Int, data: SolutionItem): Boolean =
        data is SolutionItem.SubmissionItem

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<SolutionItem> =
        ViewHolder(createView(parent, R.layout.item_solution_submission))

    private inner class ViewHolder(root: View) : DelegateViewHolder<SolutionItem>(root) {

        private val submissionRoot = root
        private val submissionQuizIcon = root.submissionQuizIcon
        private val submissionTitle = root.submissionTitle
        private val submissionStep = root.submissionStep
        private val submissionCheckBox = root.submissionCheckBox
        private val submissionStatusIconWrong = root.submissionStatusIconWrong
        private val submissionStatusIconCorrect = root.submissionStatusIconCorrect
        private val submissionStatusText = root.submissionStatusText

        init {
            root.setOnClickListener { (itemData as? SolutionItem.SubmissionItem)?.let(onItemClick) }
            submissionCheckBox.setOnClickListener {
                (itemData as? SolutionItem.SubmissionItem)?.let(onCheckboxClick)
            }
        }

        override fun onBind(data: SolutionItem) {
            data as SolutionItem.SubmissionItem

            selectionHelper.isSelected(adapterPosition).let { isSelected ->
                itemView.isSelected = isSelected
                submissionCheckBox.isChecked = isSelected
            }

            submissionRoot.isEnabled = data.isEnabled
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
            submissionStep.text =
                context.resources.getString(
                    R.string.solutions_submission_step_position,
                    data.step.position,
                    DateMapper.mapToRelativeDate(context, DateTimeHelper.nowUtc(), data.submission.time?.time ?: data.time)
                )

            when (data.submission.status) {
                Submission.Status.CORRECT -> {
                    submissionRoot.setBackgroundResource(R.drawable.bg_attempt_submission_correct_item)
                    submissionStatusText.text = context.getString(R.string.solutions_submission_correctly)
                    submissionStatusText.setTextColor(ContextCompat.getColor(context, R.color.submission_correct))
                    submissionStatusText.visibility = View.VISIBLE
                    submissionStatusIconCorrect.visibility = View.VISIBLE
                    submissionStatusIconWrong.visibility = View.GONE
                    submissionCheckBox.visibility = View.INVISIBLE
                }
                Submission.Status.WRONG -> {
                    submissionRoot.setBackgroundResource(R.drawable.bg_attempt_submission_incorrect_item)
                    submissionStatusText.text = context.getString(R.string.solutions_submission_incorrectly)
                    submissionStatusText.setTextColor(ContextCompat.getColor(context, R.color.submission_incorrect))
                    submissionStatusText.visibility = View.VISIBLE
                    submissionStatusIconCorrect.visibility = View.GONE
                    submissionStatusIconWrong.visibility = View.VISIBLE
                    submissionCheckBox.visibility = View.INVISIBLE
                }
                else -> {
                    submissionRoot.setBackgroundResource(R.drawable.bg_attempt_submission_item)
                    submissionStatusText.visibility = View.GONE
                    submissionStatusIconCorrect.visibility = View.GONE
                    submissionStatusIconWrong.visibility = View.GONE
                    submissionCheckBox.visibility = View.VISIBLE
                }
            }
        }
    }
}