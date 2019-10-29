package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import org.stepic.droid.R

class DiscardTextDialogFragment : DialogFragment() {
    companion object {
        const val TAG = "DiscardTextDialogFragment"

        fun newInstance(): DialogFragment =
            DiscardTextDialogFragment()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
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
                    getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(ContextCompat.getColor(context, R.color.new_red_color))

                    getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(ContextCompat.getColor(context, R.color.new_accent_color))
                }
            }

    interface Callback {
        fun onDiscardConfirmed()
    }
}