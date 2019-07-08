package org.stepik.android.view.step_quiz_choice.ui.delegate

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.view_choice_quiz_attempt.view.*
import org.stepik.android.model.Reply
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.presentation.step_quiz_choice.model.Choice
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz_choice.ui.adapter.ChoicesAdapterDelegate
import ru.nobird.android.ui.adapterssupport.DefaultDelegateAdapter
import ru.nobird.android.ui.adapterssupport.selection.MultipleChoiceSelectionHelper
import ru.nobird.android.ui.adapterssupport.selection.SelectionHelper
import ru.nobird.android.ui.adapterssupport.selection.SingleChoiceSelectionHelper

class ChoiceQuizFormDelegate(
    private val choiceAttemptView: View
) : StepQuizFormDelegate {

    private var choicesAdapter: DefaultDelegateAdapter<Choice> = DefaultDelegateAdapter()
    private lateinit var selectionHelper: SelectionHelper

    init {
        choiceAttemptView.choices_recycler.apply {
            adapter = choicesAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    var isEnabled: Boolean = true

    fun setAttempt(attempt: Attempt) {
        val dataSet = attempt.dataset
        dataSet?.options?.let { options ->
            choicesAdapter.items = options.map { Choice(it) }
            selectionHelper = if (dataSet.isMultipleChoice) {
                MultipleChoiceSelectionHelper(choicesAdapter)
            } else {
                SingleChoiceSelectionHelper(choicesAdapter)
            }
            choicesAdapter += ChoicesAdapterDelegate(selectionHelper, onClick = ::handleChoiceClick)
        }
    }

    fun setSubmission(submission: Submission) {
        submission.reply?.choices?.let { setChoices(it)}
    }

    override fun setState(state: StepQuizView.State.AttemptLoaded) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createReply(): ReplyResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun validateForm(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun handleChoiceClick(choice: Choice) {
        if (!isEnabled) return
        when (selectionHelper) {
            is SingleChoiceSelectionHelper -> {
                selectionHelper.reset()
                selectionHelper.select(choicesAdapter.items.indexOf(choice))
                choicesAdapter.notifyDataSetChanged()
            }
            is MultipleChoiceSelectionHelper -> {
                selectionHelper.toggle(choicesAdapter.items.indexOf(choice))
            }
        }
    }

    private fun setChoices(choices: List<Boolean>) {
        (0 until choices.size).forEach {pos ->
            if (choices[pos]) {
                selectionHelper.select(pos)
                choicesAdapter.items[pos].apply {
                    correct = true
                    // tip = "This is a tip\n new line"
                }
            }
        }
    }
}