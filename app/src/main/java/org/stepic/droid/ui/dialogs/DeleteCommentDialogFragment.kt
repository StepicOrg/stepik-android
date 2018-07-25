package org.stepic.droid.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepik.android.model.comments.Comment
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.web.Api
import org.stepic.droid.web.CommentsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class DeleteCommentDialogFragment : DialogFragment() {

    interface DialogCallback {
        fun onFailDeleteComment()

        fun onDeleteConnectionProblem()

        fun onCommentWasDeleted(comment: Comment)
    }

    @Inject
    lateinit var api: Api

    @Inject
    lateinit var analytic: Analytic

    companion object {
        private val COMMENT_ID_KEY = "comment_id_key"

        fun newInstance(commentId: Long): DialogFragment {
            val args = Bundle()
            args.putLong(COMMENT_ID_KEY, commentId)
            val fragment = DeleteCommentDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        App.component().inject(this)
        val loadingProgressDialog = LoadingProgressDialog(context)
        val commentId = arguments.getLong(COMMENT_ID_KEY)
        val builder = AlertDialog.Builder(activity)
        builder
                .setTitle(R.string.title_confirmation)
                .setMessage(R.string.delete_comment_detail)
                .setPositiveButton(R.string.delete_label) { _, _ ->
                    ProgressHelper.activate(loadingProgressDialog)
                    analytic.reportEvent(Analytic.Comments.DELETE_COMMENT_CONFIRMATION)
                    api.deleteComment(commentId).enqueue(object : Callback<CommentsResponse> {

                        override fun onResponse(call: Call<CommentsResponse>?, response: Response<CommentsResponse>?) {
                            ProgressHelper.dismiss(loadingProgressDialog)
                            if (response?.isSuccessful ?: false) {
                                val comment = response?.body()?.comments?.firstOrNull()
                                comment?.let {
                                    (targetFragment as DialogCallback).onCommentWasDeleted(it)
                                }
                            } else {
                                (targetFragment as DialogCallback).onFailDeleteComment()
                            }
                        }

                        override fun onFailure(call: Call<CommentsResponse>?, t: Throwable?) {
                            ProgressHelper.dismiss(loadingProgressDialog)
                            (targetFragment as DialogCallback).onDeleteConnectionProblem()
                        }

                    })
                }.setNegativeButton(R.string.cancel, null)

        return builder.create()
    }
}