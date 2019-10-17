package org.stepik.android.view.step_quiz_matching.ui.delegate

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.android.synthetic.main.fragment_step_quiz.view.*
import kotlinx.android.synthetic.main.layout_step_quiz_sorting.view.*
import org.stepic.droid.R
import org.stepic.droid.util.swap
import org.stepik.android.model.Reply
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.step_quiz.resolver.StepQuizFormResolver
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz_matching.ui.adapter.delegate.MatchingItemOptionAdapterDelegate
import org.stepik.android.view.step_quiz_matching.ui.adapter.delegate.MatchingItemTitleAdapterDelegate
import org.stepik.android.view.step_quiz_matching.ui.mapper.MatchingItemMapper
import org.stepik.android.view.step_quiz_matching.ui.model.MatchingItem
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class MatchingStepQuizFormDelegate(
    containerView: View
) : StepQuizFormDelegate {
    private val quizDescription = containerView.stepQuizDescription
    private val optionsAdapter = DefaultDelegateAdapter<MatchingItem>()
    private val matchingItemMapper = MatchingItemMapper()

    init {
        quizDescription.setText(R.string.step_quiz_matching_description)

        optionsAdapter += MatchingItemTitleAdapterDelegate()
        optionsAdapter += MatchingItemOptionAdapterDelegate(optionsAdapter, ::moveOption)

        with(containerView.sortingRecycler) {
            adapter = optionsAdapter
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(context)

            (itemAnimator as? SimpleItemAnimator)
                ?.supportsChangeAnimations = false
        }
    }

    private fun moveOption(position: Int, direction: MatchingItemOptionAdapterDelegate.SortingDirection) {
        val targetPosition =
            when (direction) {
                MatchingItemOptionAdapterDelegate.SortingDirection.UP ->
                    position - 2

                MatchingItemOptionAdapterDelegate.SortingDirection.DOWN ->
                    position + 2
            }

        optionsAdapter.items = optionsAdapter.items.swap(position, targetPosition)
        optionsAdapter.notifyItemChanged(position)
        optionsAdapter.notifyItemChanged(targetPosition)
    }

    override fun setState(state: StepQuizView.State.AttemptLoaded) {
        val matchingItems = matchingItemMapper
            .mapToMatchingItems(state.attempt, StepQuizFormResolver.isQuizEnabled(state))

        optionsAdapter.items =
            if (state.submissionState is StepQuizView.SubmissionState.Loaded) {
                val ordering = state.submissionState.submission.reply?.ordering ?: emptyList()
                matchingItems.sortedBy {
                    when (it) {
                        is MatchingItem.Title ->
                            it.id * 2

                        is MatchingItem.Option ->
                            ordering.indexOf(it.id) * 2 + 1
                    }
                }
            } else {
                matchingItems
            }
    }

    override fun createReply(): ReplyResult =
        ReplyResult.Success(Reply(
            ordering = optionsAdapter
                .items
                .filterIsInstance<MatchingItem.Option>()
                .map(MatchingItem.Option::id)
        ))
}