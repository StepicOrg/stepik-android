package org.stepic.droid.ui.fragments

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.squareup.otto.Subscribe
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.events.feedback.FeedbackFailedEvent
import org.stepic.droid.events.feedback.FeedbackInternetProblemsEvent
import org.stepic.droid.events.feedback.FeedbackSentEvent
import org.stepic.droid.ui.dialogs.LoadingProgressDialog
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.ValidatorUtil
import retrofit.Callback
import retrofit.Response
import retrofit.Retrofit

class TextFeedbackFragment : FragmentBase() {

    companion object {
        fun newInstance(): TextFeedbackFragment {
            val args = Bundle()

            val fragment = TextFeedbackFragment()
            fragment.arguments = args
            return fragment
        }
    }

    lateinit var toolbar: Toolbar
    lateinit var emailEditText: EditText
    lateinit var descriptionEditText: EditText
    lateinit var rootScrollView: ViewGroup
    var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.fragment_text_feedback, container, false)
        setHasOptionsMenu(true)
        v?.let {
            initToolbar(v)
            initTextFields(v)
            initScrollView(v)

            if (emailEditText.text.isEmpty()){
                emailEditText.requestFocus()
            }
            else{
                descriptionEditText.requestFocus()
            }
            progressDialog = LoadingProgressDialog(context)

        }
        return v
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    fun initScrollView(v: View) {
        rootScrollView = v.findViewById(R.id.root_view) as ViewGroup
        rootScrollView.setOnTouchListener { v, event ->
            if (!descriptionEditText.isFocused)
                descriptionEditText.requestFocus()
            false
        }
    }

    fun initToolbar(v: View) {
        toolbar = v.findViewById(R.id.toolbar) as Toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun initTextFields(v: View) {
        emailEditText = v.findViewById(R.id.feedback_contacts) as EditText
        emailEditText.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_SEND) {
                sendFeedback()
                true
            } else {
                false
            }
        }
        val primaryEmail = userPreferences.primaryEmail?.email
        primaryEmail?.let { emailEditText.setText(primaryEmail) }

        descriptionEditText = v.findViewById(R.id.feedback_form) as EditText
        descriptionEditText.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                showSoftKeypad(view)
            }
        }
    }

    private fun showSoftKeypad(editTextView: View) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editTextView, InputMethodManager.SHOW_IMPLICIT)

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.feedback_text_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_send -> {
                sendFeedback()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun sendFeedback() {
        hideSoftKeypad()
        val email = emailEditText.text.toString()
        val description = descriptionEditText.text.toString()
        if (email.isEmpty() || description.isEmpty()) {
            Toast.makeText(context, R.string.feedback_fill_fields, Toast.LENGTH_SHORT).show()
            return
        }
        if (!ValidatorUtil.isEmailValid(email)) {
            Toast.makeText(context, R.string.email_incorrect, Toast.LENGTH_SHORT).show()
            return
        }

        ProgressHelper.activate(progressDialog)
        shell.api.sendFeedback(email, description).enqueue(object : Callback<Void> {

            override fun onResponse(response: Response<Void>?, retrofit: Retrofit?) {
                ProgressHelper.dismiss(progressDialog)
                if (response?.isSuccess ?: false) {
                    bus.post(FeedbackSentEvent())

                } else {
                    bus.post(FeedbackFailedEvent())
                }
            }

            override fun onFailure(throwable: Throwable?) {
                ProgressHelper.dismiss(progressDialog)
                bus.post(FeedbackInternetProblemsEvent())
            }
        })
    }

    @Subscribe
    fun onFeedbackSent(event: FeedbackSentEvent) {
        Toast.makeText(context, R.string.feedback_sent, Toast.LENGTH_SHORT).show()
        shell.screenProvider.showMainFeed(activity)
    }

    @Subscribe
    fun onServerFail(event: FeedbackFailedEvent) {
        Toast.makeText(context, R.string.feedback_fail, Toast.LENGTH_LONG).show()
        analytic.reportEvent(Analytic.Feedback.FAILED_ON_SERVER)
    }

    @Subscribe
    fun onInternetProblems(event: FeedbackInternetProblemsEvent) {
        Toast.makeText(context, R.string.internet_problem, Toast.LENGTH_LONG).show()
        analytic.reportEvent(Analytic.Feedback.INTERNET_FAIL)
    }

    override fun onDestroyView() {
        emailEditText.setOnEditorActionListener(null)
        rootScrollView.setOnClickListener(null)
        descriptionEditText.onFocusChangeListener = null
        super.onDestroyView()
    }

}