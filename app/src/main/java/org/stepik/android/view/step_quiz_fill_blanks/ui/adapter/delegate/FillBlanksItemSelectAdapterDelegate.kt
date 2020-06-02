package org.stepik.android.view.step_quiz_fill_blanks.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.appcompat.widget.ListPopupWindow
import kotlinx.android.synthetic.main.item_step_quiz_fill_blanks_text.view.*
import org.stepic.droid.R
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

        init {
            stepQuizFillBlanksText.setOnClickListener(::showOptions)
        }

        override fun onBind(data: FillBlanksItem) {
            data as FillBlanksItem.Select
            stepQuizFillBlanksText.text = data.text
        }

        private fun showOptions(view: View) {
            val options = (itemData as? FillBlanksItem.Select)
                ?.options
                ?: return

            val popupWindow = ListPopupWindow(context)
            popupWindow.setAdapter(ArrayAdapter(context, R.layout.run_code_spinner_item, options))

            popupWindow.setOnItemClickListener { _, _, position, _ ->
                val text = options[position]
                onItemClicked(adapterPosition, text)
                popupWindow.dismiss()
            }

            popupWindow.anchorView = view
            popupWindow.width = context.resources.getDimensionPixelSize(R.dimen.step_quiz_full_screen_code_layout_drop_down_width)
            popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT

            popupWindow.show()
        }
    }
}