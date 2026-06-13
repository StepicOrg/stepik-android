package org.stepik.android.view.step_quiz_sorting.ui.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.stepic.droid.databinding.LayoutStepQuizSortingBinding
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz.ui.fragment.DefaultStepQuizFragment
import org.stepik.android.view.step_quiz_sorting.ui.delegate.SortingStepQuizFormDelegate
import ru.nobird.app.presentation.redux.container.ReduxView

class SortingStepQuizFragment :
    DefaultStepQuizFragment(),
    ReduxView<StepQuizFeature.State, StepQuizFeature.Action.ViewAction> {
    companion object {
        fun newInstance(stepId: Long): Fragment =
            SortingStepQuizFragment()
                .apply {
                    this.stepId = stepId
                }
    }

    private var _binding: LayoutStepQuizSortingBinding? = null
    private val binding: LayoutStepQuizSortingBinding
        get() = requireNotNull(_binding)

    override val quizViews: Array<View>
        get() = arrayOf(binding.root)

    override fun createStepView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return LayoutStepQuizSortingBinding.inflate(layoutInflater, parent, false).also {
            _binding = it
        }.root
    }

    override fun createStepQuizFormDelegate(): StepQuizFormDelegate =
        SortingStepQuizFormDelegate(
            stepQuizBinding = stepQuizBinding,
            sortingStepQuizBinding = binding,
            onQuizChanged = ::syncReplyState
        )
}
