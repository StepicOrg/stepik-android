package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import org.stepic.droid.R
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

    lateinit var goodButton: Button
    lateinit var badButton: Button

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater?.inflate(R.layout.fragment_feedback, container, false)
        v?.let {
            initButtons(v)
        }
        return v
    }

    private fun initButtons(v: View) {
        goodButton = v.findViewById(R.id.feedback_good_btn) as Button
        badButton = v.findViewById(R.id.feedback_bad_btn) as Button

        goodButton.setOnClickListener { shell.screenProvider.showStoreWithApp(activity) }
        badButton.setOnClickListener { shell.screenProvider.showTextFeedback(activity) }
    }

    private fun destroyButtons() {
        goodButton.setOnClickListener(null)
        badButton.setOnClickListener (null)
    }

    override fun onDestroyView() {
        destroyButtons()
        super.onDestroyView()
    }
}