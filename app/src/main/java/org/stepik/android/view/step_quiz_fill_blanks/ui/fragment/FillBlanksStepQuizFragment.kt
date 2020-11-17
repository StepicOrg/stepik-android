package org.stepik.android.view.step_quiz_fill_blanks.ui.fragment

import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.layout_step_quiz_fill_blanks.*
import org.stepic.droid.R
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz.ui.fragment.DefaultStepQuizFragment
import org.stepik.android.view.step_quiz_fill_blanks.ui.delegate.FillBlanksStepQuizFormDelegate
import ru.nobird.android.presentation.redux.container.ReduxView

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

    override val quizLayoutRes: Int =
        R.layout.layout_step_quiz_fill_blanks

    override val quizViews: Array<View>
        get() = arrayOf(fillBlanksRecycler)

    override fun createStepQuizFormDelegate(view: View): StepQuizFormDelegate {
        fillBlanksStepQuizFormDelegate = FillBlanksStepQuizFormDelegate(view, childFragmentManager)
        return fillBlanksStepQuizFormDelegate
    }

    override fun onSyncInputItemWithParent(index: Int, text: String) {
        fillBlanksStepQuizFormDelegate.updateInputItem(index, text)
    }
}