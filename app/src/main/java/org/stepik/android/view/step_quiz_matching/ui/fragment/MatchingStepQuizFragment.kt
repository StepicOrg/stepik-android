package org.stepik.android.view.step_quiz_matching.ui.fragment

import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.layout_step_quiz_sorting.*
import org.stepic.droid.R
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

    override val quizLayoutRes: Int =
        R.layout.layout_step_quiz_sorting

    override val quizViews: Array<View>
        get() = arrayOf(sortingRecycler)

    override fun createStepQuizFormDelegate(view: View): StepQuizFormDelegate =
        MatchingStepQuizFormDelegate(view)
}