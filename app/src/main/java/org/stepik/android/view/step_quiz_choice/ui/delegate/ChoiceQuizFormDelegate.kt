package org.stepik.android.view.step_quiz_choice.ui.delegate

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.view_choice_quiz_attempt.view.*
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.view.step_quiz_choice.ui.adapter.ChoicesAdapterDelegate
import ru.nobird.android.ui.adapterssupport.DefaultDelegateAdapter
import ru.nobird.android.ui.adapterssupport.selection.MultipleChoiceSelectionHelper
import ru.nobird.android.ui.adapterssupport.selection.SingleChoiceSelectionHelper

class ChoiceQuizFormDelegate(
    private val choiceAttemptView: View
) : StepQuizFormDelegate() {
    private var choicesAdapter: DefaultDelegateAdapter<String> = DefaultDelegateAdapter()

    init {
        choiceAttemptView.choices_recycler.apply {
            adapter = choicesAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    override var isEnabled: Boolean = false
        set(value) {
            field = value
            choiceAttemptView.isEnabled = value
        }

    override fun setAttempt(attempt: Attempt?) {
        val dataSet = attempt?.dataset
        dataSet?.options?.let { options ->
            choicesAdapter.items = options
            val selectionHelper = if (dataSet.isMultipleChoice) {
                SingleChoiceSelectionHelper(choicesAdapter)
            } else {
                MultipleChoiceSelectionHelper(choicesAdapter)
            }
            choicesAdapter += ChoicesAdapterDelegate(selectionHelper) { selectionHelper.select(choicesAdapter.items.indexOf(it)) }
        }

    }

    override fun setSubmission(submission: Submission?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}