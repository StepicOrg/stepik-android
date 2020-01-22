package org.stepik.android.view.attempts.ui.adapter.delegate

import android.graphics.PorterDuff
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.item_attempt_submission.view.*
import org.stepic.droid.R
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.view.attempts.model.AttemptCacheItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import java.util.TimeZone

class AttemptSubmissionAdapterDelegate : AdapterDelegate<AttemptCacheItem, DelegateViewHolder<AttemptCacheItem>>() {
    override fun isForViewType(position: Int, data: AttemptCacheItem): Boolean =
        data is AttemptCacheItem.SubmissionItem

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<AttemptCacheItem> =
        ViewHolder(createView(parent, R.layout.item_attempt_submission))

    private inner class ViewHolder(root: View) : DelegateViewHolder<AttemptCacheItem>(root) {

        private val submissionQuizIcon = root.submissionQuizIcon
        private val submissionTitle = root.submissionTitle
        private val submissionTime = root.submissionTime

        override fun onBind(data: AttemptCacheItem) {
            data as AttemptCacheItem.SubmissionItem
            val icon = AppCompatResources
                .getDrawable(context, R.drawable.ic_easy_quiz)
                ?.mutate()
            icon?.setColorFilter(
                ContextCompat.getColor(context, R.color.new_accent_color), PorterDuff.Mode.SRC_IN)
            submissionQuizIcon.setImageDrawable(icon)
            submissionTitle.text = "Quiz"
            submissionTime.text = DateTimeHelper.getPrintableDate(data.time, DateTimeHelper.DISPLAY_DATETIME_PATTERN, TimeZone.getDefault())
        }
    }
}