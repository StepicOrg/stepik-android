package org.stepik.android.view.step_quiz_fill_blanks.ui.adapter.delegate

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.graphics.drawable.DrawableCompat
import kotlinx.android.synthetic.main.item_step_quiz_fill_blanks_select.view.*
import org.stepic.droid.R
import org.stepik.android.view.step_quiz_choice.ui.delegate.LayerListDrawableDelegate
import org.stepik.android.view.step_quiz_fill_blanks.ui.model.FillBlanksItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class FillBlanksItemSelectAdapterDelegate(
    private val onItemClicked: (Int, String) -> Unit
) : AdapterDelegate<FillBlanksItem, DelegateViewHolder<FillBlanksItem>>() {
    override fun isForViewType(position: Int, data: FillBlanksItem): Boolean =
        data is FillBlanksItem.Select

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<FillBlanksItem> =
        ViewHolder(createView(parent, R.layout.item_step_quiz_fill_blanks_select))

    private inner class ViewHolder(root: View) : DelegateViewHolder<FillBlanksItem>(root) {
        private val stepQuizFillBlanksText = root.stepQuizFillBlanksText
        private val layerListDrawableDelegate: LayerListDrawableDelegate

        init {
            stepQuizFillBlanksText.setOnClickListener(::showOptions)

            layerListDrawableDelegate = LayerListDrawableDelegate(
                listOf(
                    R.id.checked_layer,
                    R.id.correct_layer,
                    R.id.incorrect_layer
                ),
                stepQuizFillBlanksText.background.mutate() as LayerDrawable
            )
        }

        override fun onBind(data: FillBlanksItem) {
            data as FillBlanksItem.Select
            itemView.isEnabled = data.isEnabled
            stepQuizFillBlanksText.text = data.text
            val (@IdRes layer, @DrawableRes statusIconRes, @ColorRes arrowColorRes) = when (data.correct) {
                true ->
                    Triple(R.id.correct_layer, R.drawable.ic_step_quiz_correct, R.color.color_correct_arrow_down)

                false ->
                    Triple(R.id.incorrect_layer, R.drawable.ic_step_quiz_wrong, R.color.color_wrong_arrow_down)

                else ->
                    Triple(R.id.checked_layer, null, R.color.color_enabled_arrow_down)
            }

            layerListDrawableDelegate.showLayer(layer)
            val iconDrawable = statusIconRes?.let { AppCompatResources.getDrawable(context, it) }
            val arrowDrawable = prepareArrowIcon(arrowColorRes)

            stepQuizFillBlanksText.setCompoundDrawablesWithIntrinsicBounds(iconDrawable, null, arrowDrawable, null)
        }

        private fun prepareArrowIcon(arrowColorRes: Int): Drawable? =
            AppCompatResources
                .getDrawable(context, R.drawable.ic_arrow_bottom)
                ?.let(DrawableCompat::wrap)
                ?.let(Drawable::mutate)
                ?.also { DrawableCompat.setTintList(it, AppCompatResources.getColorStateList(context, arrowColorRes))  }

        private fun showOptions(view: View) {
            val options = (itemData as? FillBlanksItem.Select)
                ?.options
                ?: return

            val popupWindow = ListPopupWindow(context)
            popupWindow.setAdapter(ArrayAdapter(context, R.layout.item_fill_blanks_select_spinner, options))

            popupWindow.setOnItemClickListener { _, _, position, _ ->
                val text = options[position]
                onItemClicked(adapterPosition, text)
                popupWindow.dismiss()
            }

            popupWindow.anchorView = view
            val displayMetrics = DisplayMetrics()
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val calculatedWidth = displayMetrics.widthPixels - context.resources.getDimensionPixelSize(R.dimen.step_quiz_fill_blanks_select_popup_margin)
            popupWindow.width = minOf(calculatedWidth, context.resources.getDimensionPixelSize(R.dimen.step_quiz_fill_blanks_select_popup_max_width))
            popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT

            popupWindow.show()
        }
    }
}