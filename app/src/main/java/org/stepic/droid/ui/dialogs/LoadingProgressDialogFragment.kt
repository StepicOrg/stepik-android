package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.stepic.droid.R

class LoadingProgressDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(): DialogFragment =
            LoadingProgressDialogFragment()

        const val TAG = "LoadingProgressDialogFragment"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false

        return MaterialAlertDialogBuilder(context)
            .setTitle(R.string.loading)
            .setView(R.layout.dialog_progress)
            .setCancelable(false)
            .create()
    }
}
