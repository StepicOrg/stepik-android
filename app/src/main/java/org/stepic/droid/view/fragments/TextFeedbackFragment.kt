package org.stepic.droid.view.fragments

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    lateinit var mToolbar : Toolbar

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val  v = inflater?.inflate(R.layout.fragment_text_feedback, container, false)
        v?.let {
            initToolbar(v)
        }
        return v
    }

    fun initToolbar(v : View) {
        mToolbar = v.findViewById(R.id.toolbar) as Toolbar
        (activity as AppCompatActivity).setSupportActionBar(mToolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}