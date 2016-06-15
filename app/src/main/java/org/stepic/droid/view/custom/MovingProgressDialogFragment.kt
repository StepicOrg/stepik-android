package org.stepic.droid.view.custom

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import org.stepic.droid.R

class MovingProgressDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        return LoadingProgressDialog(context, R.string.moving)
    }

    companion object {
        fun newInstance(): DialogFragment {
            val fragment = MovingProgressDialogFragment()
            return fragment
        }
    }
}
