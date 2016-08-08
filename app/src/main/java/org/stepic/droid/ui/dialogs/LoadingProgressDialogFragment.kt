package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment

class LoadingProgressDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        return LoadingProgressDialog(context)
    }

    companion object {
        fun newInstance(): DialogFragment {
            val fragment = LoadingProgressDialogFragment()
            return fragment
        }
    }
}
