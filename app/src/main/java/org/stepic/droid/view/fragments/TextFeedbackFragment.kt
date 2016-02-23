package org.stepic.droid.view.fragments

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase

class TextFeedbackFragment : FragmentBase() {

    companion object {
        fun newInstance(): TextFeedbackFragment {
            val args = Bundle()

            val fragment = TextFeedbackFragment()
            fragment.arguments = args
            return fragment
        }
    }

    lateinit var mToolbar: Toolbar
    lateinit var mEmailEditText: EditText
    lateinit var mContactsEditText: EditText
    lateinit var mSendButton: Button

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.fragment_text_feedback, container, false)
        setHasOptionsMenu(true)
        v?.let {
            initToolbar(v)
            initTextFields(v)
            initButton(v)
            v.findViewById(R.id.root_view).requestFocus()
        }
        return v
    }

    fun initToolbar(v: View) {
        mToolbar = v.findViewById(R.id.toolbar) as Toolbar
        (activity as AppCompatActivity).setSupportActionBar(mToolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun initTextFields(v: View) {
        mEmailEditText = v.findViewById(R.id.feedback_contacts) as EditText
        mEmailEditText.setOnEditorActionListener { textView, i, keyEvent ->
            if (i == EditorInfo.IME_ACTION_SEND) {
                sendFeedback()
                true
            }
            false
        }

        mContactsEditText = v.findViewById(R.id.feedback_contacts) as EditText
        val primaryEmail = mUserPreferences.primaryEmail?.email
        primaryEmail?.let { mContactsEditText.setText(primaryEmail) }
    }

    fun initButton(v: View) {
        mSendButton = v.findViewById(R.id.feedback_send_button) as Button
        mSendButton.setOnClickListener { sendFeedback() }
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
        //todo implement
        hideSoftKeypad()
        Toast.makeText(context, "feedback is sent", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        mEmailEditText.setOnEditorActionListener(null)
        mSendButton.setOnClickListener(null)
        super.onDestroyView()
    }

}