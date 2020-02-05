package org.stepik.android.view.attempts.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import org.stepic.droid.R
import java.util.ArrayList

class RemoveCachedAttemptsDialog : DialogFragment() {
    companion object {
        const val TAG = "RemoveCachedAttemptsDialog"

        private const val ARG_ATTEMPT_IDS = "attempt_ids"

        fun newInstance(attemptIds: List<Long>): RemoveCachedAttemptsDialog =
            RemoveCachedAttemptsDialog()
                .apply {
                    arguments = Bundle(1)
                        .also {
                            it.putSerializable(ARG_ATTEMPT_IDS, ArrayList(attemptIds))
                        }
                }
    }

    private val attemptsIds: List<Long>? by lazy { arguments?.getSerializable(ARG_ATTEMPT_IDS) as? List<Long> }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog
            .Builder(requireContext())
            .setTitle(R.string.solutions_delete_dialog_title)
            .setMessage(R.string.solutionss_delete_dialog_description)
            .setPositiveButton(R.string.delete_label) { _, _ ->
                val callback = activity as? Callback
                    ?: return@setPositiveButton
                attemptsIds?.let(callback::onAttemptRemoveConfirmed)
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

    interface Callback {
        fun onAttemptRemoveConfirmed(attemptIds: List<Long>)
    }
}