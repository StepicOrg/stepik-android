package org.stepik.android.view.step_quiz_table.ui.delegate

import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_step_quiz.view.*
import kotlinx.android.synthetic.main.layout_step_quiz_table.view.*
import org.stepic.droid.R
import org.stepik.android.model.Cell
import org.stepik.android.model.Reply
import org.stepik.android.model.TableChoiceAnswer
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz.model.ReplyResult
import org.stepik.android.view.step_quiz.resolver.StepQuizFormResolver
import org.stepik.android.view.step_quiz.ui.delegate.StepQuizFormDelegate
import org.stepik.android.view.step_quiz_table.ui.adapter.delegate.TableSelectionItemAdapterDelegate
import org.stepik.android.view.step_quiz_table.ui.fragment.TableColumnSelectionBottomSheetDialogFragment
import org.stepik.android.view.step_quiz_table.ui.mapper.TableSelectionItemMapper
import org.stepik.android.view.step_quiz_table.ui.model.TableSelectionItem
import ru.nobird.android.core.model.mutate
import ru.nobird.android.ui.adapters.DefaultDelegateAdapter
import ru.nobird.android.view.base.ui.extension.showIfNotExists

class TableStepQuizFormDelegate(
    containerView: View,
    private val fragmentManager: FragmentManager
) : StepQuizFormDelegate {
    private val quizDescription = containerView.stepQuizDescription

    private val tableAdapter = DefaultDelegateAdapter<TableSelectionItem>()

    private val tableSelectionItemMapper = TableSelectionItemMapper()

    private var isCheckBox: Boolean = false

    init {
        quizDescription.setText(R.string.step_quiz_table_description)

        tableAdapter += TableSelectionItemAdapterDelegate() { index, rowTitle, chosenColumns ->
            TableColumnSelectionBottomSheetDialogFragment
                .newInstance(index, rowTitle, chosenColumns, isCheckBox)
                .showIfNotExists(fragmentManager, TableColumnSelectionBottomSheetDialogFragment.TAG)
        }
        with(containerView.tableRecycler) {
            itemAnimator = null
            adapter = tableAdapter
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(context)

            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                AppCompatResources.getDrawable(context, R.drawable.bg_divider_vertical)?.let(::setDrawable)
            })
        }
    }

    override fun setState(state: StepQuizView.State.AttemptLoaded) {
        val submission = (state.submissionState as? StepQuizView.SubmissionState.Loaded)
            ?.submission

        isCheckBox = state.attempt.dataset?.isCheckbox ?: false
        tableAdapter.items = tableSelectionItemMapper.mapToTableSelectionItems(state.attempt, submission, StepQuizFormResolver.isQuizEnabled(state))
    }

    override fun createReply(): ReplyResult =
        ReplyResult.Success(Reply(
            tableChoices = tableAdapter
                .items
                .map { TableChoiceAnswer(nameRow = it.titleText, columns = it.tableChoices) }
        ))

    fun updateTableSelectionItem(index: Int, columns: List<Cell>) {
        tableAdapter.items = tableAdapter.items.mutate {
            val tableSelectionItem = get(index)
            set(index, tableSelectionItem.copy(tableChoices = columns))
        }
    }
}