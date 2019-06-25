package org.stepik.android.view.step_quiz_choice.ui.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.stepic.droid.R
import ru.nobird.android.ui.adapterdelegatessupport.AdapterDelegate
import ru.nobird.android.ui.adapterdelegatessupport.DelegateViewHolder
import ru.nobird.android.ui.adapterssupport.selection.SelectionHelper

class ChoicesAdapterDelegate(
    private val selectionHelper: SelectionHelper,
    private val onClick: (String) -> Unit
): AdapterDelegate<String, DelegateViewHolder<String>>() {
    override fun isForViewType(position: Int, data: String): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<String> =
        ViewHolder(createView(parent, R.layout.item_choice_quiz))

    inner class ViewHolder(
        containerView: View
    ) : DelegateViewHolder<String>(containerView) {

        init {
            containerView.setOnClickListener { onClick(itemData as String) }
        }

        override fun onBind(data: String) {
            val choiceText = itemView.findViewById(R.id.item_choice_text) as TextView
            itemView.isSelected = selectionHelper.isSelected(adapterPosition)
            choiceText.text = data
        }
    }
}