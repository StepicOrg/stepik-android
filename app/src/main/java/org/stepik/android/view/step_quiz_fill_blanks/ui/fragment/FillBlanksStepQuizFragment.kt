package org.stepik.android.view.step_quiz_fill_blanks.ui.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.stepic.droid.databinding.LayoutStepQuizFillBlanksBinding
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz.ui.fragment.DefaultStepQuizFragment
import org.stepik.android.view.step_quiz_fill_blanks.ui.delegate.FillBlanksStepQuizFormDelegate
import ru.nobird.app.presentation.redux.container.ReduxView

class FillBlanksStepQuizFragment :
    DefaultStepQuizFragment(),
    ReduxView<StepQuizFeature.State, StepQuizFeature.Action.ViewAction>,
    FillBlanksInputBottomSheetDialogFragment.Callback {
    companion object {
        fun newInstance(stepId: Long): Fragment =
            FillBlanksStepQuizFragment()
                .apply {
                    this.stepId = stepId
                }
    }

    private lateinit var fillBlanksStepQuizFormDelegate: FillBlanksStepQuizFormDelegate

    private var _binding: LayoutStepQuizFillBlanksBinding? = null
    private val binding: LayoutStepQuizFillBlanksBinding
        get() = requireNotNull(_binding)

    override val quizViews: Array<View>
        get() = arrayOf(binding.root)

    override fun createStepView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return LayoutStepQuizFillBlanksBinding.inflate(layoutInflater, parent, false).also {
            _binding = it
        }.root
    }

    override fun createStepQuizFormDelegate(): StepQuizFormDelegate {
        fillBlanksStepQuizFormDelegate = FillBlanksStepQuizFormDelegate(
            stepQuizBinding = stepQuizBinding,
            fillBlanksBinding = binding,
            fragmentManager = childFragmentManager,
            onQuizChanged = ::syncReplyState,
        )
        return fillBlanksStepQuizFormDelegate
    }

    override fun onSyncInputItemWithParent(index: Int, text: String) {
        fillBlanksStepQuizFormDelegate.updateInputItem(index, text)
    }
}
