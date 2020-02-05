package org.stepik.android.view.solutions.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import org.stepic.droid.R
import ru.nobird.android.view.base.ui.extension.argument

class RemoveSolutionsDialog : DialogFragment() {
    companion object {
        const val TAG = "RemoveSolutionsDialog"

        private const val ARG_ATTEMPT_IDS = "attempt_ids"

        fun newInstance(attemptIds: List<Long>): RemoveSolutionsDialog =
            RemoveSolutionsDialog()
                .apply {
                    this.attemptIds = attemptIds
                }
    }

    private var attemptIds: List<Long> by argument()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog
            .Builder(requireContext())
            .setTitle(R.string.solutions_delete_dialog_title)
            .setMessage(R.string.solutions_delete_dialog_description)
            .setPositiveButton(R.string.delete_label) { _, _ ->
                val callback = activity as? Callback
                    ?: return@setPositiveButton
                callback.onAttemptRemoveConfirmed(attemptIds)
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

    interface Callback {
        fun onAttemptRemoveConfirmed(attemptIds: List<Long>)
    }
}