package org.stepik.android.view.comment.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import org.stepic.droid.R
import org.stepic.droid.util.argument

class DeleteCommentDialogFragment : DialogFragment() {
    companion object {
        const val TAG = "DeleteCommentDialogFragment"

        fun newInstance(commentId: Long): DialogFragment =
            DeleteCommentDialogFragment().also {
                it.commentId = commentId
            }
    }

    private var commentId by argument<Long>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog
            .Builder(requireContext())
            .setTitle(R.string.title_confirmation)
            .setMessage(R.string.delete_comment_detail)
            .setPositiveButton(R.string.delete_label) { _, _ ->
                (activity as? Callback
                    ?: parentFragment as? Callback
                    ?: targetFragment as? Callback)
                    ?.onDeleteComment(commentId)
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

    interface Callback {
        fun onDeleteComment(commentId: Long)
    }
}