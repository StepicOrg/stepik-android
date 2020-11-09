package org.stepik.android.view.step_quiz_table.ui.adapter.delegate

import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_table_column_selection_radiobutton.view.*
import org.stepic.droid.R
import org.stepik.android.model.Cell
import org.stepik.android.view.latex.ui.widget.ProgressableWebViewClient
import ru.nobird.android.ui.adapterdelegates.AdapterDelegate
import ru.nobird.android.ui.adapterdelegates.DelegateViewHolder
import ru.nobird.android.ui.adapters.selection.SelectionHelper

class TableColumnSingleSelectionItemAdapterDelegate(
    private val selectionHelper: SelectionHelper,
    private val onClick: (Cell) -> Unit
) : AdapterDelegate<Cell, DelegateViewHolder<Cell>>() {
    override fun isForViewType(position: Int, data: Cell): Boolean =
        true

    override fun onCreateViewHolder(parent: ViewGroup): DelegateViewHolder<Cell> =
        ViewHolder(createView(parent, R.layout.item_table_column_selection_radiobutton))

    private inner class ViewHolder(root: View) : DelegateViewHolder<Cell>(root) {
        private val tableColumnRadioButton = root.tableColumnSelectionRadioButton
        private val tableColumnText = root.tableColumnSelectionText
        private val tableColumnTextProgress = root.tableColumnSelectionTextProgress

        init {
            root.setOnClickListener {
                onClick(itemData as Cell)
            }
            tableColumnText.webViewClient = ProgressableWebViewClient(tableColumnTextProgress, tableColumnText.webView)
        }

        override fun onBind(data: Cell) {
            itemView.isSelected = selectionHelper.isSelected(adapterPosition)
            tableColumnRadioButton.isChecked = selectionHelper.isSelected(adapterPosition)
            tableColumnText.setText(data.name)
        }
    }
}