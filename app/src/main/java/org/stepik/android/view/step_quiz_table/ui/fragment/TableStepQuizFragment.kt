package org.stepik.android.view.step_quiz_table.ui.fragment

import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.layout_step_quiz_table.*
import org.stepic.droid.R
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz.ui.fragment.DefaultStepQuizFragment
import org.stepik.android.view.step_quiz_table.ui.delegate.TableStepQuizFormDelegate

class TableStepQuizFragment : DefaultStepQuizFragment(), StepQuizView {
    companion object {
        fun newInstance(stepId: Long): Fragment =
            TableStepQuizFragment()
                .apply {
                    this.stepId = stepId
                }
    }

    override val quizLayoutRes: Int =
        R.layout.layout_step_quiz_table

    override val quizViews: Array<View>
        get() = arrayOf(tableRecycler)

    override fun createStepQuizFormDelegate(view: View): StepQuizFormDelegate =
        TableStepQuizFormDelegate(view, childFragmentManager)
}