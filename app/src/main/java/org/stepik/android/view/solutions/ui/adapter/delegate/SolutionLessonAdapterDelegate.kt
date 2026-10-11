package org.stepik.android.view.solutions.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import dev.androidbroadcast.vbpd.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemSolutionLessonBinding
import org.stepik.android.domain.solutions.model.SolutionItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.selection.SelectionHelper

class SolutionLessonAdapterDelegate(
    private val selectionHelper: SelectionHelper,
    private val onClick: (SolutionItem.LessonItem) -> Unit
) : AdapterDelegate<SolutionItem, DelegateViewHolder<SolutionItem>>() {
    override fun isForViewType(position: Int, data: SolutionItem): Boolean =
        data is SolutionItem.LessonItem

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<SolutionItem> =
        ViewHolder(createView(parent, R.layout.item_solution_lesson))

    private inner class ViewHolder(root: View) : DelegateViewHolder<SolutionItem>(root) {
        private val viewBinding: ItemSolutionLessonBinding by viewBinding { ItemSolutionLessonBinding.bind(root) }

        init {
            viewBinding.lessonCheckBox.setOnClickListener { (itemData as? SolutionItem.LessonItem)?.let(onClick) }
        }

        override fun onBind(data: SolutionItem) {
            data as SolutionItem.LessonItem
            selectionHelper.isSelected(adapterPosition).let { isSelected ->
                itemView.isSelected = isSelected
                viewBinding.lessonCheckBox.isChecked = isSelected
            }
            viewBinding.lessonCheckBox.isEnabled = data.isEnabled
            viewBinding.lessonTitle.text = context.getString(
                R.string.solutions_lesson_placeholder,
                data.section.position,
                data.unit.position,
                data.lesson.title
            )
        }
    }
}
