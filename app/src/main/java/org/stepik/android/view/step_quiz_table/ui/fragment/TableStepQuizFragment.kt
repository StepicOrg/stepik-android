package org.stepik.android.view.step_quiz_table.ui.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.stepic.droid.databinding.LayoutStepQuizTableBinding
import org.stepik.android.model.Cell
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz.ui.fragment.DefaultStepQuizFragment
import org.stepik.android.view.step_quiz_table.ui.delegate.TableStepQuizFormDelegate
import ru.nobird.app.presentation.redux.container.ReduxView

class TableStepQuizFragment :
    DefaultStepQuizFragment(),
    ReduxView<StepQuizFeature.State, StepQuizFeature.Action.ViewAction>,
    TableColumnSelectionBottomSheetDialogFragment.Callback {
    companion object {
        fun newInstance(stepId: Long): Fragment =
            TableStepQuizFragment()
                .apply {
                    this.stepId = stepId
                }
    }

    private lateinit var tableStepQuizFormDelegate: TableStepQuizFormDelegate

    private var _binding: LayoutStepQuizTableBinding? = null
    private val binding: LayoutStepQuizTableBinding
        get() = requireNotNull(_binding)

    override val quizViews: Array<View>
        get() = arrayOf(binding.root)

    override fun createStepView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return LayoutStepQuizTableBinding.inflate(layoutInflater, parent, false).also {
            _binding = it
        }.root
    }

    override fun createStepQuizFormDelegate(): StepQuizFormDelegate {
        tableStepQuizFormDelegate = TableStepQuizFormDelegate(
            stepQuizBinding = stepQuizBinding,
            tableStepQuizBinding = binding,
            fragmentManager = childFragmentManager,
            onQuizChanged = ::syncReplyState
        )
        return tableStepQuizFormDelegate
    }

    override fun onSyncChosenColumnsWithParent(index: Int, chosenRows: List<Cell>) {
        tableStepQuizFormDelegate.updateTableSelectionItem(index, chosenRows)
    }
}
