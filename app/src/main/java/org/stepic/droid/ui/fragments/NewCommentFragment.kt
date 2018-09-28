package org.stepic.droid.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import kotlinx.android.synthetic.main.new_comment_fragment.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.ui.activities.NewCommentActivity
import org.stepic.droid.ui.dialogs.DiscardTextDialogFragment
import org.stepic.droid.ui.dialogs.LoadingProgressDialog
import org.stepic.droid.ui.util.BackButtonHandler
import org.stepic.droid.ui.util.OnBackClickListener
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.web.CommentsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewCommentFragment : FragmentBase(), OnBackClickListener {

    companion object {
        private const val DISCARD_TEXT_REQUEST_CODE = 913
        private const val TARGET_KEY = "targetKey"
        private const val PARENT_KEY = "parentKey"

        fun newInstance(target: Long, parent: Long?): Fragment {
            val args = Bundle()
            if (parent != null) {
                args.putLong(PARENT_KEY, parent)
            }
            args.putLong(TARGET_KEY, target)
            val fragment = NewCommentFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var target: Long? = null
    private var parent: Long? = null
    private var isCommentSending: Boolean = false
    private lateinit var loadingProgressDialog: LoadingProgressDialog
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater.inflate(R.layout.new_comment_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        target = arguments?.getLong(TARGET_KEY)
        parent = arguments?.getLong(PARENT_KEY)
        if (parent == 0L) {
            parent = null
        }
        setHasOptionsMenu(true)
        initToolbar()
        initProgressDialog()
    }

    private fun initProgressDialog() {
        loadingProgressDialog = LoadingProgressDialog(requireContext())
    }

    override fun onResume() {
        super.onResume()
        if (!inputCommentForm.isFocused) {
            inputCommentForm.requestFocus()
        }
        inputCommentForm.postDelayed({
            showSoftKeypad(inputCommentForm)
        }, 300)
    }

    private fun initToolbar() {
        initCenteredToolbar(R.string.new_comment_title, true, -1)
    }

    private fun showSoftKeypad(editTextView: View) {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
        val text: String = textResolver.replaceWhitespaceToBr(inputCommentForm.text.toString())
        if (text.isEmpty()) {
            Toast.makeText(context, R.string.feedback_fill_fields, Toast.LENGTH_SHORT).show()
        } else if (!isCommentSending) {

            fun enableMenuItem(needEnable: Boolean = true) {
                sendMenuItem?.isEnabled = needEnable
                if (needEnable) {
                    sendMenuItem?.icon?.alpha = 255
                } else {
                    sendMenuItem?.icon?.alpha = 128
                }
            }

            isCommentSending = true
            enableMenuItem(false)
            ProgressHelper.activate(loadingProgressDialog)

            fun onFinishTryingSending() {
                isCommentSending = false
                ProgressHelper.dismiss(loadingProgressDialog)
                enableMenuItem(true)
            }

            api.postComment(text, target!!, parent).enqueue(object : Callback<CommentsResponse> {

                override fun onResponse(call: Call<CommentsResponse>?, response: Response<CommentsResponse>?) {
                    if (response?.isSuccessful ?: false && response?.body()?.comments != null) {
                        analytic.reportEvent(Analytic.Comments.COMMENTS_SENT_SUCCESSFULLY)
                        val newComment = response.body()?.comments?.firstOrNull()
                        if (newComment != null) {
                            val data = Intent()
                            //set id, target, parent
                            data.putExtra(NewCommentActivity.keyComment, newComment)
                            activity?.setResult(Activity.RESULT_OK, data)
                        }
                        Toast.makeText(App.getAppContext(), R.string.comment_sent, Toast.LENGTH_SHORT).show()
                        onFinishTryingSending()
                        activity?.finish()
                    } else {
                        Toast.makeText(App.getAppContext(), R.string.comment_denied, Toast.LENGTH_SHORT).show()
                        onFinishTryingSending()
                    }
                }

                override fun onFailure(call: Call<CommentsResponse>?, t: Throwable?) {
                    Toast.makeText(App.getAppContext(), R.string.connectionProblems, Toast.LENGTH_LONG).show()
                    onFinishTryingSending()
                }

            })
        }
    }

    override fun onBackClick(): Boolean {
        if (inputCommentForm.text?.isNotBlank() != true) {
            val dialog = DiscardTextDialogFragment.newInstance()
            dialog.setTargetFragment(this, DISCARD_TEXT_REQUEST_CODE)
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
        if (resultCode == Activity.RESULT_OK && requestCode == DISCARD_TEXT_REQUEST_CODE) {
            analytic.reportEvent(Analytic.Comments.SHOW_CONFIRM_DISCARD_TEXT_DIALOG_SUCCESS)
            activity?.finish()
        }
    }
}