package org.stepic.droid.ui.quiz

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import org.stepic.droid.R
import org.stepic.droid.ui.adapters.StepikRadioGroupAdapter
import org.stepic.droid.databinding.ViewChoiceAttemptBinding
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import ru.nobird.android.view.base.ui.extension.inflate

class ChoiceQuizDelegate: QuizDelegate() {
    private lateinit var choiceAdapter: StepikRadioGroupAdapter

    override var isEnabled: Boolean = false
        set(value) = choiceAdapter.setEnabled(value)

    override var actionButton: Button?
        get() = choiceAdapter.actionButton
        set(value) {
            choiceAdapter.actionButton = value
        }

    override fun onCreateView(parent: ViewGroup): View =
        parent.inflate(R.layout.view_choice_attempt, false)

    override fun onViewCreated(view: View) {
        choiceAdapter = StepikRadioGroupAdapter(ViewChoiceAttemptBinding.bind(view).choiceContainer)
    }

    override fun setSubmission(submission: Submission?) = choiceAdapter.setSubmission(submission)
    override fun setAttempt(attempt: Attempt?) = choiceAdapter.setAttempt(attempt)
    override fun createReply() = choiceAdapter.reply
}