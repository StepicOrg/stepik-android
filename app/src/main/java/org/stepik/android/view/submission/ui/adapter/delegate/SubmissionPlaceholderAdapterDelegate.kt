package org.stepik.android.view.submission.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import org.stepic.droid.R
import org.stepik.android.domain.submission.model.SubmissionItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class SubmissionPlaceholderAdapterDelegate : AdapterDelegate<SubmissionItem, DelegateViewHolder<SubmissionItem>>() {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<SubmissionItem> =
        ViewHolder(createView(parent, R.layout.item_submission_placeholder))

    override fun isForViewType(position: Int, data: SubmissionItem): Boolean =
        data is SubmissionItem.Placeholder

    private class ViewHolder(root: View) : DelegateViewHolder<SubmissionItem>(root)
}