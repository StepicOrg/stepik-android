package org.stepik.android.view.solutions.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import dev.androidbroadcast.vbpd.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemSolutionSubmissionBinding
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.resolveResourceIdAttribute
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
        private val viewBinding: ItemSolutionSubmissionBinding by viewBinding { ItemSolutionSubmissionBinding.bind(root) }

        init {
            root.setOnClickListener { (itemData as? SolutionItem.SubmissionItem)?.let(onItemClick) }
            viewBinding.submissionCheckBox.setOnClickListener {
                (itemData as? SolutionItem.SubmissionItem)?.let(onCheckboxClick)
            }
        }

        override fun onBind(data: SolutionItem) {
            data as SolutionItem.SubmissionItem

            selectionHelper.isSelected(adapterPosition).let { isSelected ->
                itemView.isSelected = isSelected
                viewBinding.submissionCheckBox.isChecked = isSelected
            }

            itemView.isEnabled = data.isEnabled
            viewBinding.submissionCheckBox.isEnabled = data.isEnabled

            val resourceId =
                when (data.step.block?.name) {
                    AppConstants.TYPE_CODE ->
                        R.drawable.ic_hard_quiz
                    else ->
                        R.drawable.ic_easy_quiz
                }

            viewBinding.submissionQuizIcon.setImageResource(resourceId)
            viewBinding.submissionTitle.text = HtmlCompat.fromHtml(data.step.block?.text ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
            viewBinding.submissionStep.text =
                context.resources.getString(
                    R.string.solutions_submission_step_position,
                    data.step.position,
                    DateMapper.mapToRelativeDate(context, DateTimeHelper.nowUtc(), data.submission.time?.time ?: data.time)
                )

            when (data.submission.status) {
                Submission.Status.CORRECT -> {
                    itemView.setBackgroundResource(R.drawable.bg_attempt_submission_correct_item)
                    viewBinding.submissionStatusText.text = context.getString(R.string.solutions_submission_correctly)
                    viewBinding.submissionStatusText.setTextColor(ContextCompat.getColor(context, R.color.submission_correct))
                    viewBinding.submissionStatusText.visibility = View.VISIBLE
                    viewBinding.submissionStatusIconCorrect.visibility = View.VISIBLE
                    viewBinding.submissionStatusIconWrong.visibility = View.GONE
                    viewBinding.submissionCheckBox.visibility = View.INVISIBLE
                }
                Submission.Status.WRONG -> {
                    itemView.setBackgroundResource(R.drawable.bg_attempt_submission_incorrect_item)
                    viewBinding.submissionStatusText.text = context.getString(R.string.solutions_submission_incorrectly)
                    viewBinding.submissionStatusText.setTextColor(ContextCompat.getColor(context, R.color.submission_incorrect))
                    viewBinding.submissionStatusText.visibility = View.VISIBLE
                    viewBinding.submissionStatusIconCorrect.visibility = View.GONE
                    viewBinding.submissionStatusIconWrong.visibility = View.VISIBLE
                    viewBinding.submissionCheckBox.visibility = View.INVISIBLE
                }
                else -> {
                    itemView.setBackgroundResource(context.resolveResourceIdAttribute(R.attr.selectableItemBackground))
                    viewBinding.submissionStatusText.visibility = View.GONE
                    viewBinding.submissionStatusIconCorrect.visibility = View.GONE
                    viewBinding.submissionStatusIconWrong.visibility = View.GONE
                    viewBinding.submissionCheckBox.visibility = View.VISIBLE
                }
            }
        }
    }
}
