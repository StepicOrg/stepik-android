package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.persistence.files.ExternalStorageManager
import org.stepik.android.view.settings.mapper.StorageLocationDescriptionMapper
import javax.inject.Inject

class ChooseStorageDialog : DialogFragment() {
    companion object {
        fun newInstance() =
            ChooseStorageDialog()
    }

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var externalStorageManager: ExternalStorageManager

    @Inject
    internal lateinit var storageLocationDescriptionMapper: StorageLocationDescriptionMapper

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        App.component().inject(this)

        val storageOptions = externalStorageManager.getAvailableStorageLocations()
        val currentStorageLocation = externalStorageManager.getSelectedStorageLocation()

        val headers = storageOptions
            .mapIndexed(storageLocationDescriptionMapper::mapToDescription)
            .toTypedArray()

        val indexChosen = storageOptions.indexOf(currentStorageLocation)

        val youWantMoveDataDialog = WantMoveDataDialog.newInstance()
        youWantMoveDataDialog.setTargetFragment(targetFragment, WantMoveDataDialog.REQUEST_CODE)

        val adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_single_choice, headers)

        return AlertDialog
            .Builder(requireContext(), R.style.StepikTheme_LoginDialog)
            .setTitle(R.string.choose_storage_title)
            .setSingleChoiceItems(adapter, indexChosen) { _, which ->
                if (which != indexChosen && !youWantMoveDataDialog.isAdded) {
                    youWantMoveDataDialog.targetLocation = storageOptions[which]
                    youWantMoveDataDialog.show(requireFragmentManager(), null)
                }
                dismiss()
            }
            .setNegativeButton(R.string.cancel) { _, _ -> analytic.reportEvent(Analytic.Interaction.CANCEL_CHOOSE_STORE_CLICK) }
            .create()
    }
}