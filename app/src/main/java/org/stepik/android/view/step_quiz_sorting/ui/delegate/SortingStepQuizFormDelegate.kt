package org.stepik.android.view.step_quiz_sorting.ui.delegate

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
import org.stepik.android.view.step_quiz_sorting.ui.adapter.delegate.SortingOptionAdapterDelegate
import org.stepik.android.view.step_quiz_sorting.ui.mapper.SortingOptionMapper
import org.stepik.android.view.step_quiz_sorting.ui.model.SortingOption
import ru.nobird.app.core.model.swap
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class SortingStepQuizFormDelegate(
    private val quizDescription: TextView,
    private val sortingRecycler: RecyclerView,
    private val onQuizChanged: (ReplyResult) -> Unit
) : StepQuizFormDelegate {
    constructor(
        stepQuizBinding: FragmentStepQuizBinding,
        sortingStepQuizBinding: LayoutStepQuizSortingBinding,
        onQuizChanged: (ReplyResult) -> Unit
    ) : this(
        stepQuizBinding.stepQuizDescription,
        sortingStepQuizBinding.root,
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

    private val optionsAdapter = DefaultDelegateAdapter<SortingOption>()

    private val sortingOptionMapper = SortingOptionMapper()

    init {
        quizDescription.setText(R.string.step_quiz_sorting_description)

        optionsAdapter += SortingOptionAdapterDelegate(optionsAdapter, ::moveSortingOption)

        with(sortingRecycler) {
            adapter = optionsAdapter
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(context)

            (itemAnimator as? SimpleItemAnimator)
                ?.supportsChangeAnimations = false
        }
    }

    private fun moveSortingOption(position: Int, direction: SortingOptionAdapterDelegate.SortingDirection) {
        val targetPosition =
            when (direction) {
                SortingOptionAdapterDelegate.SortingDirection.UP ->
                    position - 1

                SortingOptionAdapterDelegate.SortingDirection.DOWN ->
                    position + 1
            }

        optionsAdapter.items = optionsAdapter.items.swap(position, targetPosition)
        optionsAdapter.notifyItemChanged(position)
        optionsAdapter.notifyItemChanged(targetPosition)
        onQuizChanged(createReply())
    }

    override fun setState(state: StepQuizFeature.State.AttemptLoaded) {
        val sortingOptions = sortingOptionMapper
            .mapToSortingOptions(state.attempt, StepQuizFormResolver.isQuizEnabled(state))

        optionsAdapter.items =
            if (state.submissionState is StepQuizFeature.SubmissionState.Loaded) {
                val ordering = state.submissionState.submission.reply?.ordering ?: emptyList()
                sortingOptions.sortedBy { ordering.indexOf(it.id) }
            } else {
                sortingOptions
            }
    }

    override fun createReply(): ReplyResult =
        ReplyResult(Reply(ordering = optionsAdapter.items.map(SortingOption::id)), ReplyResult.Validation.Success)
}
