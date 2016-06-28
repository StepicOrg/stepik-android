package org.stepic.droid.view.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.squareup.otto.Bus
import com.yandex.metrica.YandexMetrica
import org.stepic.droid.R
import org.stepic.droid.base.MainApplication
import org.stepic.droid.events.comments.FailDeleteCommentEvent
import org.stepic.droid.events.comments.InternetConnectionProblemInCommentsEvent
import org.stepic.droid.events.comments.NewCommentWasAddedOrUpdateEvent
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.view.custom.LoadingProgressDialog
import org.stepic.droid.web.CommentsResponse
import org.stepic.droid.web.IApi
import retrofit.Callback
import retrofit.Response
import retrofit.Retrofit
import javax.inject.Inject

class DeleteCommentDialogFragment : DialogFragment() {

    @Inject
    lateinit var api: IApi;

    @Inject
    lateinit var bus: Bus;

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
        MainApplication.component().inject(this)
        val loadingProgressDialog = LoadingProgressDialog(context)
        val commentId = arguments.getLong(COMMENT_ID_KEY)
        val builder = AlertDialog.Builder(activity)
        builder
                .setTitle(R.string.title_confirmation)
                .setMessage(R.string.delete_comment_detail)
                .setPositiveButton(R.string.delete_label) { dialog, which ->
                    ProgressHelper.activate(loadingProgressDialog)
                    YandexMetrica.reportEvent(AppConstants.DELETE_COMMENT_CONFIRMATION)
                    api.deleteComment(commentId).enqueue(object : Callback<CommentsResponse> {
                        override fun onResponse(response: Response<CommentsResponse>?, retrofit: Retrofit?) {
                            ProgressHelper.dismiss(loadingProgressDialog)
                            if (response?.isSuccess ?: false) {
                                val comment = response?.body()?.comments?.firstOrNull()
                                comment?.let {
                                    bus.post(NewCommentWasAddedOrUpdateEvent(it.target!!, it))
                                }
                            } else {
                                bus.post(FailDeleteCommentEvent())
                            }
                        }

                        override fun onFailure(t: Throwable?) {
                            ProgressHelper.dismiss(loadingProgressDialog)
                            bus.post(InternetConnectionProblemInCommentsEvent(null))
                        }

                    })
                }.setNegativeButton(R.string.cancel, null)

        return builder.create()
    }
}