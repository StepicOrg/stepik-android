package org.stepik.android.view.step_quiz_choice.ui.adapter

import android.graphics.drawable.GradientDrawable
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.stepic.droid.R
import org.stepic.droid.util.DpPixelsHelper
import org.stepik.android.presentation.step_quiz_choice.model.Choice
import org.stepik.android.presentation.step_quiz_choice.model.ChoiceColor
import ru.nobird.android.ui.adapterdelegatessupport.AdapterDelegate
import ru.nobird.android.ui.adapterdelegatessupport.DelegateViewHolder
import ru.nobird.android.ui.adapterssupport.selection.SelectionHelper

class ChoicesAdapterDelegate(
    private val selectionHelper: SelectionHelper,
    private val onClick: (Choice) -> Unit
): AdapterDelegate<Choice, DelegateViewHolder<Choice>>() {
    override fun isForViewType(position: Int, data: Choice): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<Choice> =
        ViewHolder(createView(parent, R.layout.item_choice_quiz))

    inner class ViewHolder(
        containerView: View
    ) : DelegateViewHolder<Choice>(containerView) {

        init {
            containerView.setOnClickListener { onClick(itemData as Choice) }
        }

        override fun onBind(data: Choice) {
            itemView as TextView
            itemView.isSelected = selectionHelper.isSelected(adapterPosition)
            itemView.text = data.option
            itemView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            bindColor(data)
        }

        private fun bindColor(data: Choice) {
            val shape = itemView.background as GradientDrawable
            val choiceColor = inferChoiceColor(data)
            shape.setColor(ContextCompat.getColor(itemView.context, choiceColor.backgroundColor))
            shape.setStroke(DpPixelsHelper.convertDpToPixel(1f).toInt(), ContextCompat.getColor(itemView.context, choiceColor.strokeColor))
        }

        private fun inferChoiceColor(data: Choice): ChoiceColor =
            if (itemView.isSelected) {
                when (data.correct) {
                    true -> {
                        itemView as TextView
                        itemView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_correct_checkmark, 0)
                        ChoiceColor.CORRECT
                    }
                    false -> {
                        ChoiceColor.INCORRECT
                    }
                    else -> {
                        ChoiceColor.CHECKED
                    }
                }
            } else {
                ChoiceColor.NOT_CHECKED
            }
    }
}