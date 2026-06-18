package org.stepik.android.view.step_quiz_text.ui.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.stepic.droid.databinding.LayoutStepQuizTextBinding
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz.ui.fragment.DefaultStepQuizFragment
import org.stepik.android.view.step_quiz_text.ui.delegate.TextStepQuizFormDelegate
import ru.nobird.app.presentation.redux.container.ReduxView

class TextStepQuizFragment :
    DefaultStepQuizFragment(),
    ReduxView<StepQuizFeature.State, StepQuizFeature.Action.ViewAction> {
    companion object {
        fun newInstance(stepId: Long): Fragment =
            TextStepQuizFragment()
                .apply {
                    this.stepId = stepId
                }
    }

    private var _binding: LayoutStepQuizTextBinding? = null
    private val binding: LayoutStepQuizTextBinding
        get() = requireNotNull(_binding)

    override val quizViews: Array<View>
        get() = arrayOf(binding.root)

    override fun createStepView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return LayoutStepQuizTextBinding.inflate(layoutInflater, parent, false).also {
            _binding = it
        }.root
    }

    override fun createStepQuizFormDelegate(): StepQuizFormDelegate =
        TextStepQuizFormDelegate(
            stepQuizBinding = stepQuizBinding,
            textStepQuizBinding = binding,
            stepBlockName = stepWrapper.step.block?.name,
            onQuizChanged = ::syncReplyState
        )
}
