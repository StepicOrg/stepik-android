package org.stepik.android.view.step_quiz_choice.ui.adapter

import android.graphics.drawable.GradientDrawable
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.stepic.droid.R
import org.stepic.droid.util.DpPixelsHelper
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
            itemView as TextView
            itemView.isSelected = selectionHelper.isSelected(adapterPosition)
            itemView.text = data
            val shape = itemView.background as GradientDrawable
            if (itemView.isSelected) {
                shape.setColor(ContextCompat.getColor(itemView.context, R.color.choice_checked))
                shape.setStroke(DpPixelsHelper.convertDpToPixel(1f).toInt(), ContextCompat.getColor(itemView.context, R.color.choice_checked_border))
            } else {
                shape.setColor(ContextCompat.getColor(itemView.context, R.color.choice_not_checked))
                shape.setStroke(DpPixelsHelper.convertDpToPixel(1f).toInt(), ContextCompat.getColor(itemView.context, R.color.choice_not_checked_border))
            }
        }
    }
}