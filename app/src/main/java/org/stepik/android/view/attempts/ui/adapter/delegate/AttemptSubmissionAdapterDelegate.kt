package org.stepik.android.view.attempts.ui.adapter.delegate

import android.graphics.PorterDuff
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import kotlinx.android.synthetic.main.item_attempt_submission.view.*
import org.stepic.droid.R
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.view.attempts.model.AttemptCacheItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.selection.SelectionHelper
import java.util.TimeZone

class AttemptSubmissionAdapterDelegate(
    private val selectionHelper: SelectionHelper,
    private val onClick: (AttemptCacheItem.SubmissionItem) -> Unit
) : AdapterDelegate<AttemptCacheItem, DelegateViewHolder<AttemptCacheItem>>() {
    override fun isForViewType(position: Int, data: AttemptCacheItem): Boolean =
        data is AttemptCacheItem.SubmissionItem

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<AttemptCacheItem> =
        ViewHolder(createView(parent, R.layout.item_attempt_submission))

    private inner class ViewHolder(root: View) : DelegateViewHolder<AttemptCacheItem>(root) {

        private val submissionQuizIcon = root.submissionQuizIcon
        private val submissionTitle = root.submissionTitle
        private val submissionStep = root.submissionStep
        private val submissionTime = root.submissionTime
        private val submissionCheckBox = root.submissionCheckBox

        init {
            root.setOnClickListener { onClick(itemData as AttemptCacheItem.SubmissionItem) }
            submissionCheckBox.setOnClickListener { onClick(itemData as AttemptCacheItem.SubmissionItem) }
        }

        override fun onBind(data: AttemptCacheItem) {
            data as AttemptCacheItem.SubmissionItem
            selectionHelper.isSelected(adapterPosition).let { isSelected ->
                itemView.isSelected = isSelected
                submissionCheckBox.isChecked = isSelected
            }
            itemView.isSelected = selectionHelper.isSelected(adapterPosition)
            val icon = AppCompatResources
                .getDrawable(context, R.drawable.ic_easy_quiz)
                ?.mutate()
            icon?.setColorFilter(
                ContextCompat.getColor(context, R.color.new_accent_color), PorterDuff.Mode.SRC_IN)
            submissionQuizIcon.setImageDrawable(icon)
            submissionTitle.text =  HtmlCompat.fromHtml(data.step.block?.text ?: "", HtmlCompat.FROM_HTML_MODE_LEGACY)
            submissionStep.text = context.resources.getString(R.string.attempts_submission_step_position, data.step.position)
            submissionTime.text = DateTimeHelper.getPrintableDate(data.time, DateTimeHelper.DISPLAY_DATETIME_PATTERN, TimeZone.getDefault())
        }
    }
}