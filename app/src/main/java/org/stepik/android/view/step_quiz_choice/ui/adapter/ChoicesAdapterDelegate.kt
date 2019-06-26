package org.stepik.android.view.step_quiz_choice.ui.adapter

import android.graphics.drawable.GradientDrawable
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_choice_quiz.view.*
import org.stepic.droid.R
import org.stepic.droid.util.DpPixelsHelper
import org.stepic.droid.util.setRoundedCorners
import org.stepic.droid.util.setTopRoundedCorners
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
        root: View
    ) : DelegateViewHolder<Choice>(root) {

        private val itemChoiceText = root.itemChoiceText
        private val itemChoiceTip  = root.itemChoiceTip

        init {
            root.setOnClickListener { onClick(itemData as Choice) }
        }

        override fun onBind(data: Choice) {
            itemView.isSelected = selectionHelper.isSelected(adapterPosition)
            itemChoiceText.apply {
                text = data.option
                setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
            bindBackground(data)
        }

        private fun bindBackground(data: Choice) {
            val shape = itemChoiceText.background as GradientDrawable
            val choiceColor = inferChoiceColor(data)
            bindTip(data)
            setChoiceCorners(shape, data.tip != null)
            shape.setColor(ContextCompat.getColor(itemView.context, choiceColor.backgroundColor))
            shape.setStroke(
                DpPixelsHelper.convertDpToPixel(itemView.context.resources.getDimension(R.dimen.choice_option_stroke_width)).toInt(),
                ContextCompat.getColor(itemView.context, choiceColor.strokeColor)
            )
        }

        private fun setChoiceCorners(shape: GradientDrawable, hasTip: Boolean) {
            if (hasTip) {
                shape.setTopRoundedCorners(DpPixelsHelper.convertDpToPixel(8f))
            } else {
                shape.setRoundedCorners(DpPixelsHelper.convertDpToPixel(8f))
            }
        }

        private fun bindTip(data: Choice) {
            if (data.tip == null) {
                itemChoiceTip.visibility = View.GONE

            } else {
                itemChoiceTip.apply {
                    visibility = View.VISIBLE
                    text = data.tip
                }
            }
        }

        private fun inferChoiceColor(data: Choice): ChoiceColor =
            if (itemView.isSelected) {
                when (data.correct) {
                    true -> {
                        itemChoiceText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_correct_checkmark, 0)
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