package org.stepik.android.view.step_quiz_matching.ui.delegate

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import org.stepic.droid.R
import org.stepic.droid.databinding.FragmentStepQuizBinding
import org.stepic.droid.databinding.LayoutStepQuizSortingBinding
import org.stepik.android.model.Reply
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.step_quiz.resolver.StepQuizFormResolver
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz_matching.ui.adapter.delegate.MatchingItemOptionAdapterDelegate
import org.stepik.android.view.step_quiz_matching.ui.adapter.delegate.MatchingItemTitleAdapterDelegate
import org.stepik.android.view.step_quiz_matching.ui.mapper.MatchingItemMapper
import org.stepik.android.view.step_quiz_matching.ui.model.MatchingItem
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.app.core.model.swap

class MatchingStepQuizFormDelegate(
    private val quizDescription: TextView,
    private val sortingRecycler: RecyclerView,
    private val onQuizChanged: (ReplyResult) -> Unit
) : StepQuizFormDelegate {
    constructor(
        stepQuizBinding: FragmentStepQuizBinding,
        matchingStepQuizBinding: LayoutStepQuizSortingBinding,
        onQuizChanged: (ReplyResult) -> Unit
    ) : this(
        stepQuizBinding.stepQuizDescription,
        matchingStepQuizBinding.root,
        onQuizChanged
    )

    constructor(
        containerView: View,
        onQuizChanged: (ReplyResult) -> Unit
    ) : this(
        containerView.findViewById(R.id.stepQuizDescription),
        containerView.findViewById(R.id.sortingRecycler),
        onQuizChanged
    )

    private val optionsAdapter = DefaultDelegateAdapter<MatchingItem>()
    private val matchingItemMapper = MatchingItemMapper()

    init {
        quizDescription.setText(R.string.step_quiz_matching_description)

        optionsAdapter += MatchingItemTitleAdapterDelegate()
        optionsAdapter += MatchingItemOptionAdapterDelegate(optionsAdapter, ::moveOption)

        with(sortingRecycler) {
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
        onQuizChanged(createReply())
    }

    override fun setState(state: StepQuizFeature.State.AttemptLoaded) {
        val matchingItems = matchingItemMapper
            .mapToMatchingItems(state.attempt, StepQuizFormResolver.isQuizEnabled(state))

        optionsAdapter.items =
            if (state.submissionState is StepQuizFeature.SubmissionState.Loaded) {
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
        ReplyResult(
            Reply(
                ordering = optionsAdapter
                    .items
                    .filterIsInstance<MatchingItem.Option>()
                    .map(MatchingItem.Option::id)
            ),
            ReplyResult.Validation.Success
        )
}
