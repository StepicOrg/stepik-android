package org.stepic.droid.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.stepic.droid.R

class ClearVideosDialog : DialogFragment() {
    companion object {
        const val TAG = "ClearVideosDialog"
        const val REQUEST_CODE = 1599

        fun newInstance(): ClearVideosDialog =
                ClearVideosDialog()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_confirmation)
            .setMessage(R.string.clear_videos)
            .setPositiveButton(R.string.yes) { _, _ ->
                targetFragment?.onActivityResult(REQUEST_CODE, Activity.RESULT_OK, null)
            }
            .setNegativeButton(R.string.no, null)
            .create()
}
