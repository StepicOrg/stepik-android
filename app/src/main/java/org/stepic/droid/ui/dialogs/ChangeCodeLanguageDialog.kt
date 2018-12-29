package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import org.stepic.droid.R

class ChangeCodeLanguageDialog : DialogFragment() {
    companion object {
        fun newInstance(): ChangeCodeLanguageDialog = ChangeCodeLanguageDialog()
    }

    interface Callback {
        fun onChangeLanguage()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            AlertDialog.Builder(requireContext())
                    .setTitle(R.string.reset_code_dialog_title)
                    .setMessage(R.string.change_code_dialog_explanation)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        (parentFragment as Callback).onChangeLanguage()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .create()
}

