package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
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

        return AlertDialog.Builder(context)
                .setTitle(R.string.choose_storage_title)
                .setNegativeButton(R.string.cancel) { _, _ -> analytic.reportEvent(Analytic.Interaction.CANCEL_CHOOSE_STORE_CLICK) }
                .setSingleChoiceItems(headers, indexChosen) { _, which ->
                    if (which != indexChosen && !youWantMoveDataDialog.isAdded) {
                        youWantMoveDataDialog.targetLocation = storageOptions[which]
                        youWantMoveDataDialog.show(fragmentManager, null)
                    }
                    dismiss()
                }
                .create()
    }
}