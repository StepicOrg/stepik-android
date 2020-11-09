package org.stepik.android.view.step_quiz_fill_blanks.ui.delegate

import android.view.View
import androidx.fragment.app.FragmentManager
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.android.synthetic.main.fragment_step_quiz.view.*
import kotlinx.android.synthetic.main.layout_step_quiz_fill_blanks.view.*
import org.stepic.droid.R
import ru.nobird.android.core.model.mutate
import org.stepik.android.model.Reply
import org.stepik.android.presentation.step_quiz.StepQuizFeature
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.step_quiz.resolver.StepQuizFormResolver
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz_fill_blanks.ui.adapter.delegate.FillBlanksItemInputAdapterDelegate
import org.stepik.android.view.step_quiz_fill_blanks.ui.adapter.delegate.FillBlanksItemSelectAdapterDelegate
import org.stepik.android.view.step_quiz_fill_blanks.ui.adapter.delegate.FillBlanksItemTextAdapterDelegate
import org.stepik.android.view.step_quiz_fill_blanks.ui.fragment.FillBlanksInputBottomSheetDialogFragment
import org.stepik.android.view.step_quiz_fill_blanks.ui.mapper.FillBlanksItemMapper
import org.stepik.android.view.step_quiz_fill_blanks.ui.model.FillBlanksItem
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.extension.showIfNotExists

class FillBlanksStepQuizFormDelegate(
    private val containerView: View,
    private val fragmentManager: FragmentManager
) : StepQuizFormDelegate {
    private val quizDescription = containerView.stepQuizDescription
    private val itemsAdapter = DefaultDelegateAdapter<FillBlanksItem>()
    private val fillBlanksItemMapper = FillBlanksItemMapper()

    init {
        quizDescription.setText(R.string.step_quiz_fill_blanks_description)

        itemsAdapter += FillBlanksItemTextAdapterDelegate()
        itemsAdapter += FillBlanksItemInputAdapterDelegate(onItemClicked = ::inputItemAction)
        itemsAdapter += FillBlanksItemSelectAdapterDelegate(onItemClicked = ::selectItemAction)

        with(containerView.fillBlanksRecycler) {
            itemAnimator = null
            adapter = itemsAdapter
            isNestedScrollingEnabled = false
            layoutManager = FlexboxLayoutManager(context)
        }
    }

    fun updateInputItem(index: Int, text: String) {
        itemsAdapter.items = itemsAdapter.items.mutate {
            val inputItem = get(index) as FillBlanksItem.Input
            set(index, inputItem.copy(text = text))
        }
        itemsAdapter.notifyItemChanged(index)
    }

    private fun selectItemAction(index: Int, text: String) {
        itemsAdapter.items = itemsAdapter.items.mutate {
            val selectItem = get(index) as FillBlanksItem.Select
            set(index, selectItem.copy(text = text))
        }
    }

    private fun inputItemAction(index: Int, text: String) {
        FillBlanksInputBottomSheetDialogFragment
            .newInstance(index, text)
            .showIfNotExists(fragmentManager, FillBlanksInputBottomSheetDialogFragment.TAG)
    }

    override fun setState(state: StepQuizFeature.State.AttemptLoaded) {
        val submission = (state.submissionState as? StepQuizFeature.SubmissionState.Loaded)
            ?.submission

        itemsAdapter.items = fillBlanksItemMapper.mapToFillBlanksItems(state.attempt, submission, StepQuizFormResolver.isQuizEnabled(state))
        containerView.post { containerView.fillBlanksRecycler.requestLayout() }
    }

    override fun createReply(): ReplyResult =
        ReplyResult.Success(Reply(
            blanks = itemsAdapter
                .items
                .mapNotNull { item ->
                    when (item) {
                        is FillBlanksItem.Text ->
                            null

                        is FillBlanksItem.Input ->
                            item.text

                        is FillBlanksItem.Select ->
                            item.text
                    }
                }
        ))
}