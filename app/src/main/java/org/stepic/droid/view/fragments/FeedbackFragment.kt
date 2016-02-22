package org.stepic.droid.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.stepic.droid.base.FragmentBase

class FeedbackFragment : FragmentBase() {
    companion object {
        fun newInstance(): FeedbackFragment {
            val args = Bundle()

            val fragment = FeedbackFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}