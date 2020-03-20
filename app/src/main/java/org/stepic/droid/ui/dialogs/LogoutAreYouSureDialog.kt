package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.stepic.droid.R

class LogoutAreYouSureDialog : DialogFragment() {
    companion object {
        const val TAG = "logout_are_you_sure_dialog"

        fun newInstance(): LogoutAreYouSureDialog =
            LogoutAreYouSureDialog()

        interface OnLogoutSuccessListener {
            fun onLogout()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.title_confirmation)
            .setMessage(R.string.are_you_sure_logout)
            .setPositiveButton(R.string.yes) { _, _ ->
                (targetFragment as? OnLogoutSuccessListener)
                    ?.onLogout()
            }
            .setNegativeButton(R.string.no, null)
            .create()
}
