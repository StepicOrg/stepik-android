package org.stepic.droid.ui.fragments

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.view_table_quiz_layout.view.*
import org.stepic.droid.R
import org.stepic.droid.ui.adapters.TableChoiceAdapter
import org.stepic.droid.ui.decorators.GridDividerItemDecoration
import org.stepic.droid.util.DpPixelsHelper
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.model.Reply
import org.stepik.android.model.TableChoiceAnswer
import java.util.ArrayList

class TableChoiceStepFragment: StepAttemptFragment() {
    companion object {
        fun newInstance(): TableChoiceStepFragment =
                TableChoiceStepFragment()
    }

    private lateinit var recyclerContainer: RecyclerView

    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var adapter: TableChoiceAdapter

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tableQuizView = layoutInflater.inflate(R.layout.view_table_quiz_layout, attemptContainer, false)
        val dp8inPx = DpPixelsHelper.convertDpToPixel(8f).toInt()
        attemptContainer.setPadding(0, dp8inPx, 0, dp8inPx)

        recyclerContainer = tableQuizView.recycler
        recyclerContainer.isNestedScrollingEnabled = false
        recyclerContainer.addItemDecoration(GridDividerItemDecoration(context))

        attemptContainer.addView(tableQuizView)
    }

    override fun showAttempt(attempt: Attempt) {
        val dataset = attempt.dataset ?: return
        val rows = dataset.rows ?: return
        val columns = dataset.columns ?: return
        val description = dataset.description ?: return
        val isCheckbox = dataset.isCheckbox

        val answerList = initAnswerListFromAttempt(rows, columns)

        gridLayoutManager = GridLayoutManager(context, rows.size + 1, GridLayoutManager.HORIZONTAL, false)
        adapter = TableChoiceAdapter(activity, rows, columns, description, isCheckbox, answerList)
        recyclerContainer.layoutManager = gridLayoutManager
        recyclerContainer.adapter = adapter
    }

    private fun initAnswerListFromAttempt(rows: List<String>, columns: List<String>): MutableList<TableChoiceAnswer> {
        // may be we should do it on background thread, but then we should rewrite logic of parent class, and do same actions for each quiz on background thread
        val result = ArrayList<TableChoiceAnswer>(rows.size)
        for (nameRow in rows) {
            val oneRowAnswer = ArrayList<TableChoiceAnswer.Cell>(columns.size)
            //we should create new objects for each try –> it is generated in for cycle (but Strings is same objects)
            for (nameColumn in columns) {
                oneRowAnswer.add(TableChoiceAnswer.Cell(nameColumn, false))
            }
            result.add(TableChoiceAnswer(nameRow, oneRowAnswer))
        }

        return result
    }

    override fun generateReply() =
            Reply(tableChoices = adapter.answers)

    override fun blockUIBeforeSubmit(needBlock: Boolean) {
        adapter.setAllItemsEnabled(!needBlock)
    }

    override fun onRestoreSubmission() {
        val choices = submission.reply?.tableChoices ?: return
        adapter.answers = choices
    }
}