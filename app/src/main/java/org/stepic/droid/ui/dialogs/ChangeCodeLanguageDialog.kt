package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.stepic.droid.R

class ChangeCodeLanguageDialog : DialogFragment() {
    companion object {
        fun newInstance(): ChangeCodeLanguageDialog =
            ChangeCodeLanguageDialog()
    }

    interface Callback {
        fun onChangeLanguage()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.reset_code_dialog_title)
            .setMessage(R.string.change_code_dialog_explanation)
            .setPositiveButton(R.string.yes) { _, _ ->
                (parentFragment as Callback).onChangeLanguage()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
}

