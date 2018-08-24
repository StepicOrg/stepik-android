package org.stepic.droid.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import org.stepic.droid.R

class CancelVideosDialog : DialogFragment() {
    companion object {
        const val TAG = "CancelVideosDialog"
        const val REQUEST_CODE = 8449

        fun newInstance(): CancelVideosDialog = CancelVideosDialog()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = AlertDialog
            .Builder(activity)
            .setTitle(R.string.title_confirmation)
            .setMessage(R.string.are_you_sure)
            .setPositiveButton(R.string.yes) { _, _ ->
                targetFragment?.onActivityResult(REQUEST_CODE, Activity.RESULT_OK, null)
            }
            .setNegativeButton(R.string.no, null)
            .create()
}