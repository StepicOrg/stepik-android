package org.stepik.android.view.attempts.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_attempt_lesson.view.*
import org.stepic.droid.R
import org.stepik.android.domain.attempts.model.AttemptCacheItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.selection.SelectionHelper

class AttemptLessonAdapterDelegate(
    private val selectionHelper: SelectionHelper,
    private val onClick: (AttemptCacheItem.LessonItem) -> Unit
) : AdapterDelegate<AttemptCacheItem, DelegateViewHolder<AttemptCacheItem>>() {
    override fun isForViewType(position: Int, data: AttemptCacheItem): Boolean =
        data is AttemptCacheItem.LessonItem

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<AttemptCacheItem> =
        ViewHolder(createView(parent, R.layout.item_attempt_lesson))

    private inner class ViewHolder(root: View) : DelegateViewHolder<AttemptCacheItem>(root) {

        private val lessonTitle = root.lessonTitle
        private val lessonCheckBox = root.lessonCheckBox

        init {
            lessonCheckBox.setOnClickListener { onClick(itemData as AttemptCacheItem.LessonItem) }
        }

        override fun onBind(data: AttemptCacheItem) {
            data as AttemptCacheItem.LessonItem
            selectionHelper.isSelected(adapterPosition).let { isSelected ->
                itemView.isSelected = isSelected
                lessonCheckBox.isChecked = isSelected
            }
            lessonCheckBox.isEnabled = data.isEnabled
            lessonTitle.text = context.getString(
                R.string.solutions_lesson_placeholder,
                data.section.position,
                data.unit.position,
                data.lesson.title
            )
        }
    }
}