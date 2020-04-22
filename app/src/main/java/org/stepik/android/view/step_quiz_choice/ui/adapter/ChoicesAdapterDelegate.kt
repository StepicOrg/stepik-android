package org.stepik.android.view.step_quiz_choice.ui.adapter

import android.graphics.drawable.LayerDrawable
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.item_step_quiz_choice.view.*
import org.stepic.droid.R
import org.stepik.android.view.latex.ui.widget.ProgressableWebViewClient
import org.stepik.android.view.step_quiz_choice.model.Choice
import org.stepik.android.view.step_quiz_choice.ui.delegate.LayerListDrawableDelegate
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.selection.SelectionHelper

class ChoicesAdapterDelegate(
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
        private val itemChoiceLatexProgress = root.itemChoiceLatexProgress
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
                itemChoiceContainer.background.mutate() as LayerDrawable
            )
            itemChoiceLatex.webViewClient = ProgressableWebViewClient(itemChoiceLatexProgress, itemChoiceLatex.webView)

            ViewCompat.setBackgroundTintList(
                itemChoiceFeedback, AppCompatResources.getColorStateList(context, R.color.color_elevation_overlay_1dp))
        }

        override fun onBind(data: Choice) {
            itemView.itemChoiceContainer.isEnabled = data.isEnabled
            itemView.isSelected = selectionHelper.isSelected(adapterPosition)
            itemChoiceCheckmark.isInvisible = data.correct != true
            itemChoiceLatex.setText(data.option)
            layerListDrawableDelegate.showLayer(getItemBackgroundLayer(data))
            bindHint(data)
        }

        private fun bindHint(data: Choice) {
            itemChoiceFeedback.isVisible = !data.feedback.isNullOrEmpty()
            itemChoiceFeedback.setText(data.feedback)
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