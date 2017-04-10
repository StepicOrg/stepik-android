package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_feedback.*
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

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?)
            = inflater?.inflate(R.layout.fragment_feedback, container, false)

    override fun onViewCreated(v: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        v?.let {
            initButtons()
        }
    }

    private fun initButtons() {
        feedbackGoodButton.setOnClickListener { screenManager.showStoreWithApp(activity) }
        feedbackBadButton.setOnClickListener { screenManager.showTextFeedback(activity) }
    }

    private fun destroyButtons() {
        feedbackGoodButton.setOnClickListener(null)
        feedbackBadButton.setOnClickListener(null)
    }

    override fun onDestroyView() {
        destroyButtons()
        super.onDestroyView()
    }
}