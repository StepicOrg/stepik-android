package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.core.app.DialogFragment
import android.support.v7.app.AlertDialog
import org.stepic.droid.R

class AllowMobileDataDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(): AllowMobileDataDialogFragment =
                AllowMobileDataDialogFragment()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            AlertDialog.Builder(requireContext())
                    .setTitle(R.string.allow_mobile_download_title)
                    .setMessage(R.string.allow_mobile_message)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        //mobile allowed
                        (targetFragment as Callback).onMobileDataStateChanged(true)
                    }
                    .setNegativeButton(R.string.no) { _, _ ->
                        //only wifi allowed
                        (targetFragment as Callback).onMobileDataStateChanged(false)
                    }
                    .create()

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        (targetFragment as Callback).onMobileDataStateChanged(false)
    }

    /**
     * The callback should be implemented by target fragment
     */
    interface Callback {
        fun onMobileDataStateChanged(isMobileAllowed: Boolean)
    }
}