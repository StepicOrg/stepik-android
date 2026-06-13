package org.stepik.android.view.step_quiz_matching.ui.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.stepic.droid.databinding.LayoutStepQuizSortingBinding
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz.ui.fragment.DefaultStepQuizFragment
import org.stepik.android.view.step_quiz_matching.ui.delegate.MatchingStepQuizFormDelegate

class MatchingStepQuizFragment : DefaultStepQuizFragment() {
    companion object {
        fun newInstance(stepId: Long): Fragment =
            MatchingStepQuizFragment()
                .apply {
                    this.stepId = stepId
                }
    }

    private var _binding: LayoutStepQuizSortingBinding? = null
    private val binding
        get() = requireNotNull(_binding)

    override val quizViews: Array<View>
        get() = arrayOf(binding.root)

    override fun createStepView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return LayoutStepQuizSortingBinding.inflate(layoutInflater, parent, false).also {
            _binding = it
        }.root
    }

    override fun createStepQuizFormDelegate(): StepQuizFormDelegate =
        MatchingStepQuizFormDelegate(
            stepQuizBinding = stepQuizBinding,
            matchingStepQuizBinding = binding,
            onQuizChanged = ::syncReplyState
        )
}
