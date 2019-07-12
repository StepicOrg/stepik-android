package org.stepik.android.view.step_quiz_choice.ui.adapter

import android.graphics.drawable.LayerDrawable
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_step_quiz_choice.view.*
import org.stepic.droid.R
import org.stepic.droid.fonts.FontType
import org.stepic.droid.fonts.FontsProvider
import org.stepik.android.view.step_quiz_choice.model.Choice
import org.stepik.android.view.step_quiz_choice.ui.delegate.LayerListDrawableDelegate
import ru.nobird.android.ui.adapterdelegatessupport.AdapterDelegate
import ru.nobird.android.ui.adapterdelegatessupport.DelegateViewHolder
import ru.nobird.android.ui.adapterssupport.selection.SelectionHelper

class ChoicesAdapterDelegate(
    private val fontsProvider: FontsProvider,
    private val selectionHelper: SelectionHelper,
    private val onClick: (Choice) -> Unit
) : AdapterDelegate<Choice, DelegateViewHolder<Choice>>() {
    override fun isForViewType(position: Int, data: Choice): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<Choice> =
        ViewHolder(createView(parent, R.layout.item_step_quiz_choice))

    inner class ViewHolder(
        root: View
    ) : DelegateViewHolder<Choice>(root) {

        private val itemChoiceContainer = root.itemChoiceContainer
        private val itemChoiceCheckmark = root.itemChoiceCheckmark
        private val itemChoiceLatex = root.itemChoiceLatex
        private val itemChoiceFeedback  = root.itemChoiceFeedback
        private val layerListDrawableDelegate: LayerListDrawableDelegate

        init {
            root.itemChoiceContainer.setOnClickListener {
                if (it.isEnabled) {
                    onClick(itemData as Choice)
                }
            }
            layerListDrawableDelegate = LayerListDrawableDelegate(
                listOf(
                    R.id.not_checked_layer,
                    R.id.not_checked_layer_with_hint,
                    R.id.checked_layer,
                    R.id.correct_layer,
                    R.id.incorrect_layer,
                    R.id.incorrect_layer_with_hint
                ),
                itemChoiceContainer.background.mutate() as LayerDrawable)

            itemChoiceFeedback.setTextSize(14f)
            itemChoiceFeedback.setBackgroundResource(R.drawable.bg_step_quiz_choice_item_feedback)
        }

        override fun onBind(data: Choice) {
            itemView.itemChoiceContainer.isEnabled = data.isEnabled
            itemView.isSelected = selectionHelper.isSelected(adapterPosition)
            itemChoiceCheckmark.visibility = if (data.correct == true) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
            itemChoiceLatex.setAnyText(data.option)
            layerListDrawableDelegate.showLayer(getItemBackgroundLayer(data))
            bindHint(data)
        }

        private fun bindHint(data: Choice) {
            if (data.feedback.isNullOrEmpty()) {
                itemChoiceFeedback.visibility = View.GONE
            } else {
                itemChoiceFeedback.visibility = View.VISIBLE
                itemChoiceFeedback.setPlainOrLaTeXTextWithCustomFontColored(
                    data.feedback, fontsProvider.provideFontPath(FontType.mono),
                    R.color.new_accent_color,
                    true
                )
            }
        }

        private fun getItemBackgroundLayer(data: Choice): Int =
            if (itemView.isSelected) {
                when (data.correct) {
                    true ->
                        R.id.correct_layer

                    false ->
                        if (data.feedback.isNullOrEmpty()) {
                            R.id.incorrect_layer
                        } else {
                            R.id.incorrect_layer_with_hint
                        }

                    else ->
                        R.id.checked_layer
                }
            } else {
                if (data.feedback.isNullOrEmpty()) {
                    R.id.not_checked_layer
                } else {
                    R.id.not_checked_layer_with_hint
                }
            }
    }
}