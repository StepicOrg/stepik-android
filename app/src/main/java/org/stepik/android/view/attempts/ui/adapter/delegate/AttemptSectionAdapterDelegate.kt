package org.stepik.android.view.attempts.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_attempt_section.view.*
import org.stepic.droid.R
import org.stepik.android.view.attempts.model.AttemptCacheItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class AttemptSectionAdapterDelegate : AdapterDelegate<AttemptCacheItem, DelegateViewHolder<AttemptCacheItem>>() {
    override fun isForViewType(position: Int, data: AttemptCacheItem): Boolean =
        data is AttemptCacheItem.SectionItem

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<AttemptCacheItem> =
        ViewHolder(createView(parent, R.layout.item_attempt_section))

    private inner class ViewHolder(root: View) : DelegateViewHolder<AttemptCacheItem>(root) {

        private val sectionTitle = root.sectionTitle

        override fun onBind(data: AttemptCacheItem) {
            data as AttemptCacheItem.SectionItem
            sectionTitle.text = data.section.title
        }
    }
}