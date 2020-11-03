package org.stepik.android.view.step_quiz_table.ui.delegate

import android.view.View
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.fragment_step_quiz.view.*
import org.stepic.droid.R
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate

class TableStepQuizFormDelegate(
    containerView: View,
    private val fragmentManager: FragmentManager
) : StepQuizFormDelegate {
    private val quizDescription = containerView.stepQuizDescription

    init {
        quizDescription.setText(R.string.step_quiz_table_description)
    }

    override fun setState(state: StepQuizView.State.AttemptLoaded) {
        TODO("Not yet implemented")
    }

    override fun createReply(): ReplyResult {
        TODO("Not yet implemented")
    }
}