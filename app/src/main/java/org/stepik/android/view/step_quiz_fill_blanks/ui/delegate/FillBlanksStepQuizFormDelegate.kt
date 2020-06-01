package org.stepik.android.view.step_quiz_fill_blanks.ui.delegate

import android.view.View
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.android.synthetic.main.fragment_step_quiz.view.*
import kotlinx.android.synthetic.main.layout_step_quiz_fill_blanks.view.*
import org.stepic.droid.R
import org.stepik.android.model.Reply
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.step_quiz.resolver.StepQuizFormResolver
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz_fill_blanks.ui.adapter.delegate.FillBlanksItemInputAdapterDelegate
import org.stepik.android.view.step_quiz_fill_blanks.ui.adapter.delegate.FillBlanksItemSelectAdapterDelegate
import org.stepik.android.view.step_quiz_fill_blanks.ui.adapter.delegate.FillBlanksItemTextAdapterDelegate
import org.stepik.android.view.step_quiz_fill_blanks.ui.mapper.FillBlanksItemMapper
import org.stepik.android.view.step_quiz_fill_blanks.ui.model.FillBlanksItem
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter

class FillBlanksStepQuizFormDelegate(
    containerView: View
) : StepQuizFormDelegate {
    private val quizDescription = containerView.stepQuizDescription
    private val itemsAdapter = DefaultDelegateAdapter<FillBlanksItem>()
    private val fillBlanksItemMapper = FillBlanksItemMapper()

    init {
        quizDescription.setText(R.string.step_quiz_fill_blanks_description)

        itemsAdapter += FillBlanksItemTextAdapterDelegate()
        itemsAdapter += FillBlanksItemInputAdapterDelegate()
        itemsAdapter += FillBlanksItemSelectAdapterDelegate()

        with(containerView.fillBlanksRecycler) {
            adapter = itemsAdapter
            isNestedScrollingEnabled = false
            layoutManager = FlexboxLayoutManager(context)
        }
    }

    // TODO Update mapping
    override fun setState(state: StepQuizView.State.AttemptLoaded) {
        val fillBlanksItems = fillBlanksItemMapper
            .mapToFillBlanksItems(state.attempt, StepQuizFormResolver.isQuizEnabled(state))

        itemsAdapter.items = fillBlanksItems
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