package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.stepic.droid.R
import org.stepic.droid.util.resolveAttribute

class DiscardTextDialogFragment : DialogFragment() {
    companion object {
        const val TAG = "DiscardTextDialogFragment"

        fun newInstance(): DialogFragment =
            DiscardTextDialogFragment()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.title_confirmation)
            .setMessage(R.string.are_you_sure_remove_comment_text)
            .setPositiveButton(R.string.delete_label) { _, _ ->
                (activity as? Callback
                    ?: parentFragment as? Callback
                    ?: targetFragment as? Callback)
                    ?.onDiscardConfirmed()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
            .apply {
                setOnShowListener {
                    val colorError = context
                        .resolveAttribute(R.attr.colorError)
                        ?.data
                        ?: return@setOnShowListener

                    getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(colorError)
                }
            }

    interface Callback {
        fun onDiscardConfirmed()
    }
}