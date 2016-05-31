package org.stepic.droid.view.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase

class NewCommentFragment : FragmentBase() {

    companion object {
//        private val discussionIdKey = "dis_id_key"

        fun newInstance(): Fragment {
            val args = Bundle()
//            args.putString(discussionIdKey, discussionId)
            val fragment = NewCommentFragment()
            fragment.arguments = args
            return fragment
        }
    }

    lateinit var mToolbar: Toolbar
    lateinit var mTextBody: EditText

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.new_comment_fragment, container, false)
//        discussionId = arguments.getString(CommentsFragment.discussionIdKey)
        setHasOptionsMenu(true)
        v?.let {
            initToolbar(v)
            initEditBody(v)
        }
        return v
    }

    private fun initEditBody(v: View) {
        mTextBody = v.findViewById(R.id.input_comment_form) as EditText
    }

    override fun onResume() {
        super.onResume()
        if (!mTextBody.isFocused) {
            mTextBody.requestFocus()
        }
        mTextBody.postDelayed({
            showSoftKeypad(mTextBody)
        }, 300)
    }

    private fun initToolbar(v: View) {
        mToolbar = v.findViewById(R.id.toolbar) as Toolbar
        (activity as AppCompatActivity).setSupportActionBar(mToolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)
    }

    private fun showSoftKeypad(editTextView: View) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editTextView, InputMethodManager.SHOW_IMPLICIT)

    }
}