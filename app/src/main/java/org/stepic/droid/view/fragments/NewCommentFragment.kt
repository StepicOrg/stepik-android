package org.stepic.droid.view.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.new_comment_fragment, container, false)
//        discussionId = arguments.getString(CommentsFragment.discussionIdKey)
        setHasOptionsMenu(true)
        v?.let {
            initToolbar(v)
        }
        return v
    }

    private fun initToolbar(v: View) {
        mToolbar = v.findViewById(R.id.toolbar) as Toolbar
        (activity as AppCompatActivity).setSupportActionBar(mToolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)
    }
}