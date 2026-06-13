package org.stepik.android.view.solutions.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemSolutionSectionBinding
import org.stepik.android.domain.solutions.model.SolutionItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.selection.SelectionHelper

class SolutionSectionAdapterDelegate(
    private val selectionHelper: SelectionHelper,
    private val onClick: (SolutionItem.SectionItem) -> Unit
) : AdapterDelegate<SolutionItem, DelegateViewHolder<SolutionItem>>() {
    override fun isForViewType(position: Int, data: SolutionItem): Boolean =
        data is SolutionItem.SectionItem

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<SolutionItem> =
        ViewHolder(createView(parent, R.layout.item_solution_section))

    private inner class ViewHolder(root: View) : DelegateViewHolder<SolutionItem>(root) {
        private val viewBinding: ItemSolutionSectionBinding by viewBinding { ItemSolutionSectionBinding.bind(root) }

        init {
            viewBinding.sectionCheckBox.setOnClickListener { (itemData as? SolutionItem.SectionItem)?.let(onClick) }
        }

        override fun onBind(data: SolutionItem) {
            data as SolutionItem.SectionItem
            selectionHelper.isSelected(adapterPosition).let { isSelected ->
                itemView.isSelected = isSelected
                viewBinding.sectionCheckBox.isChecked = isSelected
            }
            viewBinding.sectionCheckBox.isEnabled = data.isEnabled
            viewBinding.sectionTitle.text = context.resources.getString(
                R.string.solutions_section_placeholder,
                data.section.position,
                data.section.title
            )
        }
    }
}
