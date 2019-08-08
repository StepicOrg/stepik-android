package org.stepik.android.view.step_quiz_sorting.ui.delegate

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.fragment_step_quiz.view.*
import kotlinx.android.synthetic.main.layout_step_quiz_sorting.view.*
import org.stepic.droid.R
import org.stepik.android.model.Reply
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz_sorting.ui.adapter.delegate.SortingOptionAdapterDelegate
import org.stepik.android.view.step_quiz_sorting.ui.mapper.SortingOptionMapper
import org.stepik.android.view.step_quiz_sorting.ui.model.SortingOption
import ru.nobird.android.ui.adapterssupport.DefaultDelegateAdapter

class SortingStepQuizFormDelegate(
    containerView: View
) : StepQuizFormDelegate {
    private val context = containerView.context

    private val quizDescription = containerView.stepQuizDescription

    private val optionsAdapter = DefaultDelegateAdapter<SortingOption>()
        .also {
            it += SortingOptionAdapterDelegate()
        }

    private val sortingOptionMapper = SortingOptionMapper()

    init {
        quizDescription.setText(R.string.step_quiz_sorting_description)

        with(containerView.sortingRecycler) {
            adapter = optionsAdapter
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun setState(state: StepQuizView.State.AttemptLoaded) {
        optionsAdapter.items = sortingOptionMapper.mapToSortingOptions(state.attempt)
    }

    override fun createReply(): ReplyResult =
        ReplyResult.Success(Reply(ordering = optionsAdapter.items.map(SortingOption::id)))
}