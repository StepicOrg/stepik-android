package org.stepik.android.view.step_quiz_choice.ui.delegate

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.fragment_step_quiz.view.*
import kotlinx.android.synthetic.main.layout_step_quiz_choice.view.*
import org.stepic.droid.R
import org.stepic.droid.fonts.FontsProvider
import org.stepik.android.model.Reply
import org.stepik.android.model.Submission
import org.stepik.android.model.feedback.ChoiceFeedback
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.presentation.step_quiz_choice.model.Choice
import org.stepik.android.view.step_quiz.resolver.StepQuizFormResolver
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz_choice.ui.adapter.ChoicesAdapterDelegate
import ru.nobird.android.ui.adapterssupport.DefaultDelegateAdapter
import ru.nobird.android.ui.adapterssupport.selection.MultipleChoiceSelectionHelper
import ru.nobird.android.ui.adapterssupport.selection.SelectionHelper
import ru.nobird.android.ui.adapterssupport.selection.SingleChoiceSelectionHelper

class ChoiceQuizFormDelegate(
    containerView: View,
    private val fontsProvider: FontsProvider
) : StepQuizFormDelegate {
    private val context = containerView.context

    private val quizDescription = containerView.stepQuizDescription
    private var choicesAdapter: DefaultDelegateAdapter<Choice> = DefaultDelegateAdapter()
    private var selectionHelper: SelectionHelper? = null

    init {
        containerView.choices_recycler.apply {
            itemAnimator = null
            adapter = choicesAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
        quizDescription.setText(R.string.step_quiz_choice_description)
    }

    override fun setState(state: StepQuizView.State.AttemptLoaded) {
        val dataset = state.attempt.dataset ?: return

        val submission = (state.submissionState as? StepQuizView.SubmissionState.Loaded)
                ?.submission

        val reply = submission?.reply

        choicesAdapter.items = dataset.options?.map { Choice(it, isEnabled = StepQuizFormResolver.isQuizEnabled(state)) } ?: return

        if (selectionHelper == null) {
            selectionHelper = if (dataset.isMultipleChoice) {
                MultipleChoiceSelectionHelper(choicesAdapter)
            } else {
                SingleChoiceSelectionHelper(choicesAdapter)
            }
        }
        if (choicesAdapter.delegates.isEmpty()) {
            choicesAdapter += ChoicesAdapterDelegate(fontsProvider, selectionHelper as SelectionHelper, onClick = ::handleChoiceClick)
        }
        selectionHelper?.reset()
        setChoices(reply?.choices, submission?.status, submission?.feedback as? ChoiceFeedback)
    }

    override fun createReply(): ReplyResult {
        val choices = (0 until choicesAdapter.itemCount).map { selectionHelper?.isSelected(it) as Boolean }
        return if (choices.contains(true)) {
            ReplyResult.Success(Reply(choices = choices))
        } else {
            ReplyResult.Error(context.getString(R.string.step_quiz_choice_empty_reply))
        }
    }

    private fun handleChoiceClick(choice: Choice) {
        when (selectionHelper) {
            is SingleChoiceSelectionHelper -> {
                selectionHelper?.select(choicesAdapter.items.indexOf(choice))
            }
            is MultipleChoiceSelectionHelper -> {
                selectionHelper?.toggle(choicesAdapter.items.indexOf(choice))
            }
        }
    }

    private fun setChoices(choices: List<Boolean>?, status: Submission.Status?, choiceFeedback: ChoiceFeedback?) {
        if (choices == null) return
        (0 until choices.size).forEach { pos ->
            if (choices[pos]) {
                selectionHelper?.select(pos)
                choicesAdapter.items[pos].apply {
                    correct = when (status) {
                        Submission.Status.CORRECT -> true
                        Submission.Status.WRONG -> false
                        else -> null
                    }
                }
            }
            choicesAdapter.items[pos].feedback = choiceFeedback?.optionsFeedback?.get(pos) ?: ""
        }
    }
}