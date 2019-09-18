package org.stepik.android.view.font_size_settings.ui.adapter

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_font_size.view.*
import org.stepic.droid.R
import org.stepik.android.view.font_size_settings.model.FontItem
import ru.nobird.android.ui.adapterdelegatessupport.AdapterDelegate
import ru.nobird.android.ui.adapterdelegatessupport.DelegateViewHolder
import ru.nobird.android.ui.adapterssupport.selection.SelectionHelper

class FontSizeDelegate(
    private val selectionHelper: SelectionHelper,
    private val onItemClicked: (FontItem) -> Unit
) : AdapterDelegate<FontItem, DelegateViewHolder<FontItem>>() {
    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<FontItem> =
        ViewHolder(createView(parent, R.layout.item_font_size))

    override fun isForViewType(position: Int, data: FontItem): Boolean =
        true

    inner class ViewHolder(root: View) : DelegateViewHolder<FontItem>(root) {

        private val checkedTextView = root.fontItem

        init {
            root.setOnClickListener { onItemClicked(itemData as FontItem) }
        }

        override fun onBind(data: FontItem) {
            val isSelected = selectionHelper.isSelected(adapterPosition)
            checkedTextView.isChecked  = isSelected
            checkedTextView.text = data.title
        }
    }
}