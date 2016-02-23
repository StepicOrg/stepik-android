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
import org.stepic.droid.util.ValidatorUtil
import retrofit.BaseUrl
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

    lateinit var mToolbar: Toolbar
    lateinit var mEmailEditText: EditText
    lateinit var mDescriptionEditTex: EditText
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
        val primaryEmail = mUserPreferences.primaryEmail?.email
        primaryEmail?.let { mEmailEditText.setText(primaryEmail) }

        mDescriptionEditTex = v.findViewById(R.id.feedback_form) as EditText
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
        val email = mEmailEditText.text.toString()
        val description = mDescriptionEditTex.text.toString()
        if (email.isEmpty() || description.isEmpty()) {
            Toast.makeText(context, "Заполните поля", Toast.LENGTH_SHORT).show()
            return
        }
        if (!ValidatorUtil.isEmailValid(email)) {
            Toast.makeText(context, R.string.email_incorrect, Toast.LENGTH_SHORT).show()
            return
        }

        mShell.api.sendFeedback(email, description).enqueue(object : Callback<Void> {

            override fun onResponse(response: Response<Void>?, retrofit: Retrofit?) {
                if (response?.isSuccess ?: false) {
                    Toast.makeText(context, "Сообщение отправлено", Toast.LENGTH_SHORT).show()//todo get from res
                } else {
                    Toast.makeText(context, "Что-то пошло не так, сервер вернул ошибку: " + response?.code(), Toast.LENGTH_SHORT).show()//todo get from res
                }
            }

            override fun onFailure(throwable: Throwable?) {
                Toast.makeText(context, "Проблемы с интернетом. Данные не отправлены.", Toast.LENGTH_SHORT).show()//todo get from res
            }


        })
    }

    override fun onDestroyView() {
        mEmailEditText.setOnEditorActionListener(null)
        mSendButton.setOnClickListener(null)
        super.onDestroyView()
    }

}