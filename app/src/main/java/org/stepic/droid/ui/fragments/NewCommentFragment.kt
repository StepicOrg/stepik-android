package org.stepic.droid.ui.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.base.MainApplication
import org.stepic.droid.events.comments.NewCommentWasAddedOrUpdateEvent
import org.stepic.droid.ui.dialogs.DiscardTextDialogFragment
import org.stepic.droid.ui.dialogs.LoadingProgressDialog
import org.stepic.droid.ui.util.BackButtonHandler
import org.stepic.droid.ui.util.OnBackClickListener
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.web.CommentsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class NewCommentFragment : FragmentBase(), OnBackClickListener {

    companion object {
        private val requestDiscardText = 913;
        private val targetKey = "targetKey"
        private val parentKey = "parentKey"

        fun newInstance(target: Long, parent: Long?): Fragment {
            val args = Bundle()
            if (parent != null) {
                args.putLong(parentKey, parent)
            }
            args.putLong(targetKey, target)
            val fragment = NewCommentFragment()
            fragment.arguments = args
            return fragment
        }
    }

    lateinit var toolbar: Toolbar
    lateinit var textBody: EditText
    var target: Long? = null
    var parent: Long? = null
    var isCommentSending: Boolean = false
    lateinit var loadingProgressDialog: ProgressDialog
    private var sendMenuItem: MenuItem? = null

    private var backButtonHandler: BackButtonHandler? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        (activity as? BackButtonHandler)?.setBackClickListener(this)
    }

    override fun onDetach() {
        backButtonHandler?.removeBackClickListener(this)
        backButtonHandler = null
        super.onDetach()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater?.inflate(R.layout.new_comment_fragment, container, false)

    override fun onViewCreated(v: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        target = arguments.getLong(NewCommentFragment.targetKey)
        parent = arguments.getLong(NewCommentFragment.parentKey)
        if (parent == 0L) {
            parent = null
        }
        setHasOptionsMenu(true)
        v?.let {
            initToolbar(v)
            initEditBody(v)
            initProgressDialog()
        }
    }

    private fun initProgressDialog() {
        loadingProgressDialog = LoadingProgressDialog(context)
    }

    private fun initEditBody(v: View) {
        textBody = v.findViewById(R.id.input_comment_form) as EditText
    }

    override fun onResume() {
        super.onResume()
        if (!textBody.isFocused) {
            textBody.requestFocus()
        }
        textBody.postDelayed({
            showSoftKeypad(textBody)
        }, 300)
    }

    private fun initToolbar(v: View) {
        toolbar = v.findViewById(R.id.toolbar) as Toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)
    }

    private fun showSoftKeypad(editTextView: View) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editTextView, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.new_comment_menu, menu)
        sendMenuItem = menu?.findItem(R.id.action_send_comment)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_send_comment -> {
                sendComment()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sendComment() {
        analytic.reportEvent(Analytic.Comments.CLICK_SEND_COMMENTS)
        val text: String = textResolver.replaceWhitespaceToBr(textBody.text.toString())
        if (text.isEmpty()) {
            Toast.makeText(context, R.string.feedback_fill_fields, Toast.LENGTH_SHORT).show()
        } else {

            fun enableMenuItem(needEnable: Boolean = true) {
                sendMenuItem?.isEnabled = needEnable
                if (needEnable) {
                    sendMenuItem?.icon?.alpha = 255;
                } else {
                    sendMenuItem?.icon?.alpha = 128;
                }
            }

            if (!isCommentSending) {
                isCommentSending = true
                enableMenuItem(false)
                ProgressHelper.activate(loadingProgressDialog)

                fun onFinishTryingSending() {
                    isCommentSending = false
                    ProgressHelper.dismiss(loadingProgressDialog)
                    enableMenuItem(true)
                }

                shell.api.postComment(text, target!!, parent).enqueue(object : Callback<CommentsResponse> {

                    override fun onResponse(call: Call<CommentsResponse>?, response: Response<CommentsResponse>?) {
                        if (response?.isSuccessful ?: false && response?.body()?.comments != null) {
                            analytic.reportEvent(Analytic.Comments.COMMENTS_SENT_SUCCESSFULLY)
                            val newComment = response?.body()?.comments?.firstOrNull()
                            bus.post(NewCommentWasAddedOrUpdateEvent(targetId = target!!, newCommentInsertOrUpdate = newComment))
                            Toast.makeText(MainApplication.getAppContext(), R.string.comment_sent, Toast.LENGTH_SHORT).show()
                            onFinishTryingSending()
                            activity?.finish()
                        } else {
                            Toast.makeText(MainApplication.getAppContext(), R.string.comment_denied, Toast.LENGTH_SHORT).show()
                            onFinishTryingSending()
                        }
                    }

                    override fun onFailure(call: Call<CommentsResponse>?, t: Throwable?) {
                        Toast.makeText(MainApplication.getAppContext(), R.string.connectionProblems, Toast.LENGTH_LONG).show()
                        onFinishTryingSending()
                    }

                })
            }
        }
    }

    override fun onBackClick(): Boolean {
        if (textBody.text?.isNotBlank() ?: false) {
            val dialog = DiscardTextDialogFragment.newInstance()
            dialog.setTargetFragment(this, requestDiscardText)
            if (!dialog.isAdded) {
                analytic.reportEvent(Analytic.Comments.SHOW_CONFIRM_DISCARD_TEXT_DIALOG)
                dialog.show(fragmentManager, null)
            }
            return true
        } else {
            return false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == requestDiscardText) {
                analytic.reportEvent(Analytic.Comments.SHOW_CONFIRM_DISCARD_TEXT_DIALOG_SUCCESS)
                activity?.finish()
            }
        }
    }
}