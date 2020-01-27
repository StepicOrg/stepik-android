package org.stepik.android.view.attempts.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_attempt_section.view.*
import org.stepic.droid.R
import org.stepik.android.view.attempts.model.AttemptCacheItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.selection.SelectionHelper

class AttemptSectionAdapterDelegate(
    private val selectionHelper: SelectionHelper,
    private val onClick: (AttemptCacheItem.SectionItem) -> Unit
) : AdapterDelegate<AttemptCacheItem, DelegateViewHolder<AttemptCacheItem>>() {
    override fun isForViewType(position: Int, data: AttemptCacheItem): Boolean =
        data is AttemptCacheItem.SectionItem

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<AttemptCacheItem> =
        ViewHolder(createView(parent, R.layout.item_attempt_section))

    private inner class ViewHolder(root: View) : DelegateViewHolder<AttemptCacheItem>(root) {

        private val sectionTitle = root.sectionTitle
        private val sectionCheckBox = root.sectionCheckBox

        init {
            root.setOnClickListener { onClick(itemData as AttemptCacheItem.SectionItem) }
            sectionCheckBox.setOnClickListener { onClick(itemData as AttemptCacheItem.SectionItem) }
        }

        override fun onBind(data: AttemptCacheItem) {
            data as AttemptCacheItem.SectionItem
            selectionHelper.isSelected(adapterPosition).let { isSelected ->
                itemView.isSelected = isSelected
                sectionCheckBox.isChecked = isSelected
            }
            sectionTitle.text = context.resources.getString(
                R.string.attempts_section_placeholder,
                data.section.position,
                data.section.title
            )
        }
    }
}