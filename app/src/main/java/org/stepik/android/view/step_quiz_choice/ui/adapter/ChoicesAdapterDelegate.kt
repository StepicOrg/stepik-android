package org.stepik.android.view.step_quiz_choice.ui.adapter

import android.graphics.drawable.LayerDrawable
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_choice_quiz.view.*
import org.stepic.droid.R
import org.stepik.android.presentation.step_quiz_choice.model.Choice
import org.stepik.android.view.step_quiz_choice.ui.delegate.LayerListDrawableDelegate
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
        private val layerListDrawableDelegate: LayerListDrawableDelegate

        init {
            root.setOnClickListener { onClick(itemData as Choice) }
            layerListDrawableDelegate = LayerListDrawableDelegate(
                listOf(
                    R.id.not_checked_layer,
                    R.id.checked_layer,
                    R.id.correct_layer,
                    R.id.incorrect_layer,
                    R.id.incorrect_layer_with_tip
                ),
                itemChoiceText.background.mutate() as LayerDrawable)
        }

        override fun onBind(data: Choice) {
            itemView.isSelected = selectionHelper.isSelected(adapterPosition)
            itemChoiceText.apply {
                text = data.option
                setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
            layerListDrawableDelegate.showLayer(inferChoiceId(data))
            bindTip(data)
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

        private fun inferChoiceId(data: Choice): Int =
            if (itemView.isSelected) {
                when (data.correct) {
                    true -> {
                        itemChoiceText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_correct_checkmark, 0)
                        R.id.correct_layer
                    }
                    false -> {
                        if (data.tip == null) {
                            R.id.incorrect_layer
                        } else {
                            R.id.incorrect_layer_with_tip
                        }
                    }
                    else -> {
                        R.id.checked_layer
                    }
                }
            } else {
                R.id.not_checked_layer
            }
    }
}