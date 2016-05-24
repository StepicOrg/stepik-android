package org.stepic.droid.view.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.stepic.droid.R
import org.stepic.droid.base.FragmentBase

class CommentsFragment : FragmentBase() {
    companion object {
        private val discussionIdKey = "dis_id_key"

        fun newInstance(discussionId: String): Fragment {
            val args = Bundle()
            args.putString(discussionIdKey, discussionId)
            val fragment = CommentsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    lateinit var mToolbar: Toolbar

    lateinit var discussionId: String

    lateinit var discussionTv : TextView

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.fragment_comments, container, false)
        discussionId = arguments.getString(discussionIdKey)
        setHasOptionsMenu(true)
        v?.let {
            initToolbar(v)
            initDiscussionTv(v)
        }
        return v
    }

    private fun initDiscussionTv(v: View) {
        discussionTv = v.findViewById(R.id.discussionTv) as TextView
        discussionTv.text = discussionId
    }

    fun initToolbar(v: View) {
        mToolbar = v.findViewById(R.id.toolbar) as Toolbar
        (activity as AppCompatActivity).setSupportActionBar(mToolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

}