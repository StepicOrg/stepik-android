package org.stepik.android.view.step_quiz_pycharm.ui.fragment

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import org.stepic.droid.R
import org.stepic.droid.databinding.FragmentStepQuizPycharmBinding

class PyCharmStepQuizFragment : Fragment() {
    companion object {
        fun newInstance(): PyCharmStepQuizFragment =
            PyCharmStepQuizFragment()
    }

    private val binding: FragmentStepQuizPycharmBinding by viewBinding(FragmentStepQuizPycharmBinding::bind)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_step_quiz_pycharm, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.stepQuizFeedback.movementMethod = LinkMovementMethod.getInstance()
    }
}
