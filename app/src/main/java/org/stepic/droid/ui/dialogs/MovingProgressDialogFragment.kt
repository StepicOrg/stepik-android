package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.core.app.DialogFragment
import org.stepic.droid.R

class MovingProgressDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(): DialogFragment =
                MovingProgressDialogFragment()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        return LoadingProgressDialog(requireContext(), R.string.moving)
    }
}
