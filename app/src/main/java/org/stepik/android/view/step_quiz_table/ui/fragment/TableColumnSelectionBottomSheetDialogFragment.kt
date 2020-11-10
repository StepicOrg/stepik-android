package org.stepik.android.view.step_quiz_table.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_dialog_table_columns_selection.*
import org.stepic.droid.R
import org.stepik.android.model.Cell
import org.stepik.android.view.step_quiz_table.ui.adapter.delegate.TableColumnMultipleSelectionItemAdapterDelegate
import org.stepik.android.view.step_quiz_table.ui.adapter.delegate.TableColumnSingleSelectionItemAdapterDelegate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.ui.adapters.selection.MultipleChoiceSelectionHelper
import ru.nobird.android.ui.adapters.selection.SelectionHelper
import ru.nobird.android.ui.adapters.selection.SingleChoiceSelectionHelper
import ru.nobird.android.view.base.ui.extension.argument

class TableColumnSelectionBottomSheetDialogFragment : BottomSheetDialogFragment() {
    companion object {
        const val TAG = "TableRowSelectionBottomSheetDialogFragment"

        private const val ARG_SELECTED = "selected"

        fun newInstance(index: Int, rowTitle: String, chosenColumns: List<Cell>, isCheckBox: Boolean): DialogFragment =
            TableColumnSelectionBottomSheetDialogFragment()
                .apply {
                    this.index = index
                    this.rowTitle = rowTitle
                    this.chosenColumns = chosenColumns
                    this.isCheckBox = isCheckBox
                }
    }

    private lateinit var selectionHelper: SelectionHelper

    private var index: Int by argument()
    private var rowTitle: String by argument()
    private var chosenColumns: List<Cell> by argument()
    private var isCheckBox: Boolean by argument()

    private val columnsAdapter = DefaultDelegateAdapter<Cell>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.ThemeOverlay_AppTheme_BottomSheetDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.bottom_sheet_dialog_table_columns_selection, container, false)

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selected = if (savedInstanceState != null) {
            savedInstanceState.getBooleanArray(ARG_SELECTED) as BooleanArray
        } else {
            chosenColumns.map { it.answer }.toBooleanArray()
        }

        tableColumnSelectionRowTitle.setText(rowTitle)
        with(tableColumnsRecycler) {
            adapter = columnsAdapter
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(context)
        }
        selectionHelper = if (isCheckBox) {
            MultipleChoiceSelectionHelper(columnsAdapter)
        } else {
            SingleChoiceSelectionHelper(columnsAdapter)
        }

        val (@StringRes description, delegate) = if (isCheckBox) {
            R.string.step_quiz_table_multiple_choice to TableColumnMultipleSelectionItemAdapterDelegate(selectionHelper, ::handleColumnSelectionClick)
        } else {
            R.string.step_quiz_table_single_choice to TableColumnSingleSelectionItemAdapterDelegate(selectionHelper, ::handleColumnSelectionClick)
        }
        tableColumnSelectionInformation.setText(description)
        columnsAdapter += delegate
        columnsAdapter.items = chosenColumns
        mapSelection(selected)
        // This is necessary to avoid java.lang.IllegalArgumentException: parameter must be a descendant of this view
        tableNested.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
    }

    override fun onPause() {
        val selected = columnsAdapter.items.mapIndexed { index, cell ->
            cell.copy(answer = selectionHelper.isSelected(index))
        }
        (parentFragment as? Callback)
            ?.onSyncChosenColumnsWithParent(index, selected)
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val select = columnsAdapter.items.mapIndexed { index, cell ->
            selectionHelper.isSelected(index)
        }
        outState.putBooleanArray(ARG_SELECTED, select.toBooleanArray())
    }

    private fun handleColumnSelectionClick(cell: Cell) {
        when (selectionHelper) {
            is SingleChoiceSelectionHelper ->
                selectionHelper.select(columnsAdapter.items.indexOf(cell))

            is MultipleChoiceSelectionHelper ->
                selectionHelper.toggle(columnsAdapter.items.indexOf(cell))
        }
    }

    private fun mapSelection(choices: BooleanArray) {
        choices.forEachIndexed { index, isSelected ->
            if (isSelected) {
                selectionHelper.select(index)
            }
        }
    }

    interface Callback {
        fun onSyncChosenColumnsWithParent(index: Int, chosenRows: List<Cell>)
    }
}