package org.stepic.droid.ui.fragments

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.appbar_only_toolbar.*
import kotlinx.android.synthetic.main.fragment_text_feedback.*
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.events.feedback.FeedbackFailedEvent
import org.stepic.droid.events.feedback.FeedbackInternetProblemsEvent
import org.stepic.droid.events.feedback.FeedbackSentEvent
import org.stepic.droid.ui.dialogs.LoadingProgressDialog
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.ValidatorUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TextFeedbackFragment : FragmentBase() {

    companion object {
        fun newInstance(): TextFeedbackFragment = TextFeedbackFragment()
    }

    var progressDialog: ProgressDialog? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater?.inflate(R.layout.fragment_text_feedback, container, false)

    override fun onViewCreated(v: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        setHasOptionsMenu(true)
        v?.let {
            initToolbar()
            initTextFields()
            initScrollView(v)

            if (feedbackContactsEditText.text.isEmpty()) {
                feedbackContactsEditText.requestFocus()
            } else {
                feedbackFormEditText.requestFocus()
            }
            progressDialog = LoadingProgressDialog(context)
        }

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
        textFeedbackRootScrollView.setOnTouchListener { _, _ ->
            if (!feedbackFormEditText.isFocused)
                feedbackFormEditText.requestFocus()
            false
        }
    }

    fun initToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(toolbar as Toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun initTextFields() {
        feedbackContactsEditText.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_SEND) {
                sendFeedback()
                true
            } else {
                false
            }
        }
        val primaryEmail = userPreferences.primaryEmail?.email
        primaryEmail?.let { feedbackContactsEditText.setText(primaryEmail) }

        feedbackFormEditText.setOnFocusChangeListener { view, hasFocus ->
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
        val email = feedbackContactsEditText.text.toString()
        val description = feedbackFormEditText.text.toString()
        if (email.isEmpty() || description.isEmpty()) {
            Toast.makeText(context, R.string.feedback_fill_fields, Toast.LENGTH_SHORT).show()
            return
        }
        if (!ValidatorUtil.isEmailValid(email)) {
            Toast.makeText(context, R.string.email_incorrect, Toast.LENGTH_SHORT).show()
            return
        }

        ProgressHelper.activate(progressDialog)
        api.sendFeedback(email, description).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>?, response: Response<Void>?) {
                ProgressHelper.dismiss(progressDialog)
                if (response?.isSuccessful ?: false) {
                    bus.post(FeedbackSentEvent())

                } else {
                    bus.post(FeedbackFailedEvent())
                }
            }

            override fun onFailure(call: Call<Void>?, t: Throwable?) {
                ProgressHelper.dismiss(progressDialog)
                bus.post(FeedbackInternetProblemsEvent())
            }
        })
    }

    @Subscribe
    fun onFeedbackSent(event: FeedbackSentEvent) {
        Toast.makeText(context, R.string.feedback_sent, Toast.LENGTH_SHORT).show()
        screenManager.showMainFeed(activity)
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
        feedbackContactsEditText.setOnEditorActionListener(null)
        textFeedbackRootScrollView.setOnClickListener(null)
        feedbackFormEditText.onFocusChangeListener = null
        super.onDestroyView()
    }

}