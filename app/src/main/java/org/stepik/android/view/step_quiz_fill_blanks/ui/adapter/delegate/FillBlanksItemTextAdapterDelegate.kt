package org.stepik.android.view.step_quiz_fill_blanks.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.android.synthetic.main.item_step_quiz_fill_blanks_text.view.*
import org.stepic.droid.R
import org.stepik.android.view.step_quiz_fill_blanks.ui.model.FillBlanksItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class FillBlanksItemTextAdapterDelegate : AdapterDelegate<FillBlanksItem, DelegateViewHolder<FillBlanksItem>>() {
    override fun isForViewType(position: Int, data: FillBlanksItem): Boolean =
        data is FillBlanksItem.Text

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<FillBlanksItem> =
        ViewHolder(createView(parent, R.layout.item_step_quiz_fill_blanks_text))

    private inner class ViewHolder(root: View) : DelegateViewHolder<FillBlanksItem>(root) {
        private val stepQuizFillBlanksText = root.stepQuizFillBlanksText

        override fun onBind(data: FillBlanksItem) {
            data as FillBlanksItem.Text
            val layoutParams = itemView.layoutParams as FlexboxLayoutManager.LayoutParams
            layoutParams.isWrapBefore = data.isWrapBefore
            itemView.layoutParams = layoutParams
            stepQuizFillBlanksText.setText(data.text)
        }
    }
}