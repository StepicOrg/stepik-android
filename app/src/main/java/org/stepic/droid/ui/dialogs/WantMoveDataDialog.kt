package org.stepic.droid.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.persistence.model.StorageLocation
import ru.nobird.android.view.base.ui.extension.argument
import javax.inject.Inject

class WantMoveDataDialog : DialogFragment() {
    companion object {
        fun newInstance(): WantMoveDataDialog =
                WantMoveDataDialog()

        const val REQUEST_CODE = 1209
        const val EXTRA_LOCATION = "target_location"
    }

    @Inject
    lateinit var analytic: Analytic

    var targetLocation by argument<StorageLocation>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        App.component().inject(this)

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_confirmation)
            .setMessage(R.string.move_data_explanation)
            .setPositiveButton(R.string.yes) { _, _ ->
                analytic.reportEvent(Analytic.Interaction.TRANSFER_DATA_YES)
                targetFragment?.onActivityResult(REQUEST_CODE, Activity.RESULT_OK, Intent().putExtra(EXTRA_LOCATION, targetLocation))
            }
            .setNegativeButton(R.string.no, null)

        return builder.create()
    }
}
