package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.persistence.files.ExternalStorageManager
import org.stepic.droid.util.TextUtil.formatBytes
import javax.inject.Inject

class ChooseStorageDialog: DialogFragment() {
    companion object {
        fun newInstance() =
                ChooseStorageDialog()
    }

    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var externalStorageManager: ExternalStorageManager

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        App.component().inject(this)

        val storageOptions = externalStorageManager.getAvailableStorageLocations()
        val currentStorageLocation = externalStorageManager.getSelectedStorageLocation()

        val freeTitle = getString(R.string.free_title)

        val headers = storageOptions.mapIndexed { i, location ->
            val title = getString(if (i == 0) R.string.default_storage else R.string.secondary_storage)
            val totalSpace = formatBytes(location.totalSpaceBytes)
            val freeSpace = formatBytes(location.freeSpaceBytes)
            "$title. $freeSpace / $totalSpace $freeTitle"
        }.toTypedArray()

        val indexChosen = storageOptions.indexOf(currentStorageLocation)

        val youWantMoveDataDialog = WantMoveDataDialog.newInstance()
        youWantMoveDataDialog.setTargetFragment(targetFragment, WantMoveDataDialog.REQUEST_CODE)

        val view = View.inflate(context, R.layout.dialog_listview, null)
        val selectionItems = view.findViewById<ListView>(R.id.listChoices)

        selectionItems.adapter = ArrayAdapter<String>(requireContext(), R.layout.simple_list_item_single_choice, headers)
        selectionItems.choiceMode = ListView.CHOICE_MODE_SINGLE
        selectionItems.setItemChecked(indexChosen, true)
        selectionItems.setOnItemClickListener { _, _, position, _ ->
            if (position != indexChosen && !youWantMoveDataDialog.isAdded) {
                youWantMoveDataDialog.targetLocation = storageOptions[position]
                youWantMoveDataDialog.show(fragmentManager, null)
            }
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.choose_storage_title)
                .setView(view)
                .setNegativeButton(R.string.cancel) { _, _ -> analytic.reportEvent(Analytic.Interaction.CANCEL_CHOOSE_STORE_CLICK) }
                .create()
    }
}