package org.stepik.android.view.step_quiz_table.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import dev.androidbroadcast.vbpd.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.ItemTableSelectionBinding
import org.stepik.android.model.Cell
import org.stepik.android.view.latex.ui.widget.ProgressableWebViewClient
import org.stepik.android.view.step_quiz_table.ui.model.TableSelectionItem
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder

class TableSelectionItemAdapterDelegate(
    private val onItemClicked: (Int, String, List<Cell>) -> Unit
) : AdapterDelegate<TableSelectionItem, DelegateViewHolder<TableSelectionItem>>() {
    companion object {
        private const val SEPARATOR = ", "
    }
    override fun isForViewType(position: Int, data: TableSelectionItem): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<TableSelectionItem> =
        ViewHolder(createView(parent, R.layout.item_table_selection))

    private inner class ViewHolder(root: View) : DelegateViewHolder<TableSelectionItem>(root) {
        private val viewBinding: ItemTableSelectionBinding by viewBinding { ItemTableSelectionBinding.bind(root) }

        init {
            viewBinding.viewOverlay.setOnClickListener { onItemClicked(adapterPosition, (itemData as TableSelectionItem).titleText, (itemData as TableSelectionItem).tableChoices) }
            viewBinding.stepQuizTableTitleText.webViewClient = ProgressableWebViewClient(viewBinding.stepQuizTitleProgress, viewBinding.stepQuizTableTitleText.webView)
            viewBinding.stepQuizTableChoiceText.webViewClient = ProgressableWebViewClient(viewBinding.stepQuizChoiceProgress, viewBinding.stepQuizTableChoiceText.webView)
        }

        override fun onBind(data: TableSelectionItem) {
            viewBinding.viewOverlay.isEnabled = data.isEnabled
            viewBinding.stepQuizTableChevron.alpha = if (data.isEnabled) 1f else 0.2f
            viewBinding.stepQuizTableTitleText.setText(data.titleText)
            val selectedChoices = data.tableChoices.filter { it.answer }

            viewBinding.stepQuizTableChoiceText.isVisible = selectedChoices.isNotEmpty()
            viewBinding.stepQuizTableChoiceText.setText(selectedChoices.joinToString(separator = SEPARATOR) { it.name })
        }
    }
}
