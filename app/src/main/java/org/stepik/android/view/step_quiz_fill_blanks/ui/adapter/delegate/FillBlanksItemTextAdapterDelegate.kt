package org.stepik.android.view.step_quiz_fill_blanks.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import dev.androidbroadcast.vbpd.viewBinding
import com.google.android.flexbox.FlexboxLayoutManager
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemStepQuizFillBlanksTextBinding
import org.stepik.android.view.step_quiz_fill_blanks.ui.model.FillBlanksItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class FillBlanksItemTextAdapterDelegate : AdapterDelegate<FillBlanksItem, DelegateViewHolder<FillBlanksItem>>() {
    override fun isForViewType(position: Int, data: FillBlanksItem): Boolean =
        data is FillBlanksItem.Text

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<FillBlanksItem> =
        ViewHolder(createView(parent, R.layout.item_step_quiz_fill_blanks_text))

    private inner class ViewHolder(root: View) : DelegateViewHolder<FillBlanksItem>(root) {
        private val viewBinding: ItemStepQuizFillBlanksTextBinding by viewBinding { ItemStepQuizFillBlanksTextBinding.bind(root) }
        private val stepQuizFillBlanksText = viewBinding.stepQuizFillBlanksText

        override fun onBind(data: FillBlanksItem) {
            data as FillBlanksItem.Text

            itemView
                .updateLayoutParams<FlexboxLayoutManager.LayoutParams> { isWrapBefore = data.isWrapBefore }
            stepQuizFillBlanksText.setText(data.text)
        }
    }
}