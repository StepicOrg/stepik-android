package org.stepic.droid.ui.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_text_feedback.*
import org.stepic.droid.R
import org.stepic.droid.base.App
import org.stepic.droid.base.FragmentBase
import org.stepic.droid.core.presenters.TextFeedbackPresenter
import org.stepic.droid.core.presenters.contracts.TextFeedbackView
import org.stepic.droid.ui.dialogs.LoadingProgressDialog
import org.stepic.droid.ui.util.initCenteredToolbar
import org.stepic.droid.util.ProgressHelper
import org.stepic.droid.util.ValidatorUtil
import javax.inject.Inject

class TextFeedbackFragment : FragmentBase(), TextFeedbackView {
    companion object {
        fun newInstance(): TextFeedbackFragment = TextFeedbackFragment()
    }

    private var progressDialog: LoadingProgressDialog? = null

    @Inject
    lateinit var textFeedbackPresenter: TextFeedbackPresenter

    override fun injectComponent() {
        App
                .Companion
                .component()
                .feedbackComponentBuilder()
                .build()
                .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_text_feedback, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        initToolbar()
        initTextFields()
        initScrollView(view)

        if (feedbackContactsEditText.text.isEmpty()) {
            feedbackContactsEditText.requestFocus()
        } else {
            feedbackFormEditText.requestFocus()
        }
        progressDialog = LoadingProgressDialog(requireContext())
    }

    override fun onStart() {
        super.onStart()
        textFeedbackPresenter.attachView(this)
    }

    override fun onStop() {
        textFeedbackPresenter.detachView(this)
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
        initCenteredToolbar(R.string.feedback_title, showHomeButton = true)
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
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
        textFeedbackPresenter.sendFeedback(email, description)
    }

    override fun onServerFail() {
        Toast.makeText(context, R.string.feedback_fail, Toast.LENGTH_LONG).show()
        ProgressHelper.dismiss(progressDialog)
    }

    override fun onInternetProblems() {
        Toast.makeText(context, R.string.internet_problem, Toast.LENGTH_LONG).show()
        ProgressHelper.dismiss(progressDialog)
    }

    override fun onFeedbackSent() {
        Toast.makeText(context, R.string.feedback_sent, Toast.LENGTH_SHORT).show()
        activity?.setResult(Activity.RESULT_OK)
        activity?.finish()
    }

    override fun onDestroyView() {
        feedbackContactsEditText.setOnEditorActionListener(null)
        textFeedbackRootScrollView.setOnClickListener(null)
        feedbackFormEditText.onFocusChangeListener = null
        super.onDestroyView()
    }

}