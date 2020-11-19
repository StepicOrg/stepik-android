package org.stepik.android.view.step_quiz_text.ui.fragment

import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.layout_step_quiz_text.*
import org.stepic.droid.R
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz.ui.fragment.DefaultStepQuizFragment
import org.stepik.android.view.step_quiz_text.ui.delegate.TextStepQuizFormDelegate
import ru.nobird.android.presentation.redux.container.ReduxView

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

    override val quizLayoutRes: Int =
        R.layout.layout_step_quiz_text

    override val quizViews: Array<View>
        get() = arrayOf(stringStepQuizField)

    override fun createStepQuizFormDelegate(view: View): StepQuizFormDelegate =
        TextStepQuizFormDelegate(view, stepWrapper.step.block?.name)
}