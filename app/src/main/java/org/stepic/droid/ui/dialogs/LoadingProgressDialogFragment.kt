package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.core.app.DialogFragment

class LoadingProgressDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(): DialogFragment =
                LoadingProgressDialogFragment()

        const val TAG = "LoadingProgressDialogFragment"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        return LoadingProgressDialog(requireContext())
    }
}
