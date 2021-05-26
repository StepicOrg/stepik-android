package org.stepik.android.view.compatibility.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.stepic.droid.R

class Android4DiscontinueDialogFragment : DialogFragment() {
    companion object {
        const val TAG = "Android4DiscontinueDialogFragment"

        fun newInstance(): Android4DiscontinueDialogFragment =
            Android4DiscontinueDialogFragment()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.android_4_discontinue_title)
            .setMessage(R.string.android_4_discontinue_message)
            .setPositiveButton(R.string.ok, null)
            .create()
}