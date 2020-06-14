package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

        return MaterialAlertDialogBuilder(context)
            .setTitle(R.string.choose_storage_title)
            .setSingleChoiceItems(headers, indexChosen) { _, which ->
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