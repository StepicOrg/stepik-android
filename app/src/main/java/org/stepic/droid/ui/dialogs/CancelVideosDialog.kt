package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import org.stepic.droid.R

class CancelVideosDialog : DialogFragment() {

    companion object {
        fun newInstance(): CancelVideosDialog
                = CancelVideosDialog()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.title_confirmation)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.yes) { _, _ ->
                    (targetFragment as? Callback)?.onCancelAllVideos()
                }
                .setNegativeButton(R.string.no, null)
        return builder.create()
    }


    interface Callback {
        fun onCancelAllVideos()
    }
}
