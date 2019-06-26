package org.stepik.android.view.step_quiz_choice.ui.delegate

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.view_choice_quiz_attempt.view.*
import org.stepik.android.model.Reply
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.view.step_quiz_choice.ui.adapter.ChoicesAdapterDelegate
import ru.nobird.android.ui.adapterssupport.DefaultDelegateAdapter
import ru.nobird.android.ui.adapterssupport.selection.MultipleChoiceSelectionHelper
import ru.nobird.android.ui.adapterssupport.selection.SelectionHelper
import ru.nobird.android.ui.adapterssupport.selection.SingleChoiceSelectionHelper

class ChoiceQuizFormDelegate(
    private val choiceAttemptView: View
) : StepQuizFormDelegate() {
    private var choicesAdapter: DefaultDelegateAdapter<String> = DefaultDelegateAdapter()
    private lateinit var selectionHelper: SelectionHelper

    init {
        choiceAttemptView.choices_recycler.apply {
            adapter = choicesAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    override var isEnabled: Boolean = true

    override fun setAttempt(attempt: Attempt?) {
        val dataSet = attempt?.dataset
        dataSet?.options?.let { options ->
            choicesAdapter.items = options
            selectionHelper = if (dataSet.isMultipleChoice) {
                MultipleChoiceSelectionHelper(choicesAdapter)
            } else {
                SingleChoiceSelectionHelper(choicesAdapter)
            }
            choicesAdapter += ChoicesAdapterDelegate(selectionHelper, onClick = ::handleChoiceClick)
        }
    }

    override fun setSubmission(submission: Submission?) {
        submission?.reply?.choices?.let { setChoices(it)}
    }

    private fun handleChoiceClick(choice: String) {
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
            }
        }
    }

    val reply: Reply
        get() {
            val selection = (0 until choicesAdapter.itemCount)
                .map {
                    selectionHelper.isSelected(it)
                }
            return Reply(choices = selection)
        }
}