package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import org.stepic.droid.R

class LogoutAreYouSureDialog : DialogFragment() {

    var listener: OnLogoutSuccessListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = context as? OnLogoutSuccessListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.title_confirmation)
                .setMessage(R.string.are_you_sure_logout)
                .setPositiveButton(R.string.yes) { dialog, which ->
                    listener?.onLogout()
                }
                .setNegativeButton(R.string.no, null)

        return builder.create()
    }

    companion object {
        fun newInstance(): LogoutAreYouSureDialog {
            return LogoutAreYouSureDialog()
        }

        interface OnLogoutSuccessListener {
            fun onLogout()
        }
    }
}
