package org.stepik.android.view.step_quiz_choice.ui.fragment

import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.layout_step_quiz_choice.*
import org.stepic.droid.R
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.step_quiz.model.StepQuizLessonData
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz.ui.fragment.DefaultStepQuizFragment
import org.stepik.android.view.step_quiz_choice.ui.delegate.ChoiceStepQuizFormDelegate

class ChoiceStepQuizFragment : DefaultStepQuizFragment(), StepQuizView {
    companion object {
        fun newInstance(stepPersistentWrapper: StepPersistentWrapper, stepQuizLessonData: StepQuizLessonData): Fragment =
            ChoiceStepQuizFragment()
                .apply {
                    this.stepWrapper = stepPersistentWrapper
                    this.stepQuizLessonData = stepQuizLessonData
                }
    }

    override val quizLayoutRes: Int =
        R.layout.layout_step_quiz_choice

    override val quizViews: Array<View>
        get() = arrayOf(choicesRecycler)

    override fun createStepQuizFormDelegate(view: View): StepQuizFormDelegate =
        ChoiceStepQuizFormDelegate(view)
}