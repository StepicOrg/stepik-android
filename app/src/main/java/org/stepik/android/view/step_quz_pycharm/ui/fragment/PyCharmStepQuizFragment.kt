package org.stepik.android.view.step_quz_pycharm.ui.fragment

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_step_quiz_pycharm.*
import org.stepic.droid.R

class PyCharmStepQuizFragment : Fragment() {
    companion object {
        fun newInstance(): PyCharmStepQuizFragment =
            PyCharmStepQuizFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_step_quiz_pycharm, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stepQuizFeedback.movementMethod = LinkMovementMethod.getInstance()
    }
}