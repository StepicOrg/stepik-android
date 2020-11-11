package org.stepik.android.view.step_quiz_table.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.item_table_selection.view.*
import org.stepic.droid.R
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
        private val viewOverlay = root.viewOverlay
        private val stepQuizTableTitle = root.stepQuizTableTitleText
        private val stepQuizTableTitleProgress = root.stepQuizTitleProgress
        private val stepQuizTableChoice = root.stepQuizTableChoiceText
        private val stepQuizTableChoiceProgress = root.stepQuizChoiceProgress
        private val stepQuizTableChevron = root.stepQuizTableChevron

        init {
            viewOverlay.setOnClickListener { onItemClicked(adapterPosition, (itemData as TableSelectionItem).titleText, (itemData as TableSelectionItem).tableChoices) }
            stepQuizTableTitle.webViewClient = ProgressableWebViewClient(stepQuizTableTitleProgress, stepQuizTableTitle.webView)
            stepQuizTableChoice.webViewClient = ProgressableWebViewClient(stepQuizTableChoiceProgress, stepQuizTableChoice.webView)
        }

        override fun onBind(data: TableSelectionItem) {
            viewOverlay.isEnabled = data.isEnabled
            stepQuizTableChevron.alpha = if (data.isEnabled) 1f else 0.2f
            stepQuizTableTitle.setText(data.titleText)
            val selectedChoices = data.tableChoices.filter { it.answer }

            stepQuizTableChoice.isVisible = selectedChoices.isNotEmpty()
            stepQuizTableChoice.setText(selectedChoices.joinToString(separator = SEPARATOR) { it.name })
        }
    }
}