package org.stepik.android.view.step_quiz_choice.ui.adapter

import android.graphics.PorterDuff
import android.graphics.drawable.LayerDrawable
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemStepQuizChoiceBinding
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

        private val viewBinding: ItemStepQuizChoiceBinding by viewBinding { ItemStepQuizChoiceBinding.bind(root) }

        private val layerListDrawableDelegate: LayerListDrawableDelegate

        init {
            viewBinding.itemChoiceContainer.setOnClickListener {
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
                viewBinding.itemChoiceContainer.background.mutate() as LayerDrawable
            )
            viewBinding.itemChoiceLatex.webViewClient = ProgressableWebViewClient(viewBinding.itemChoiceLatexProgress, viewBinding.itemChoiceLatex.webView)

            viewBinding.itemChoiceFeedback.background = AppCompatResources
                .getDrawable(context, R.drawable.bg_shape_rounded_bottom)
                ?.mutate()
                ?.let { DrawableCompat.wrap(it) }
                ?.also {
                    DrawableCompat.setTint(it, ContextCompat.getColor(context, R.color.color_elevation_overlay_1dp))
                    DrawableCompat.setTintMode(it, PorterDuff.Mode.SRC_IN)
                }
        }

        override fun onBind(data: Choice) {
            viewBinding.itemChoiceContainer.isEnabled = data.isEnabled
            itemView.isSelected = selectionHelper.isSelected(adapterPosition)
            viewBinding.itemChoiceCheckmark.isInvisible = data.correct != true
            viewBinding.itemChoiceLatex.setText(data.option)
            layerListDrawableDelegate.showLayer(getItemBackgroundLayer(data))
            bindHint(data)
        }

        private fun bindHint(data: Choice) {
            viewBinding.itemChoiceFeedback.isVisible = !data.feedback.isNullOrEmpty()
            viewBinding.itemChoiceFeedback.setText(data.feedback)
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