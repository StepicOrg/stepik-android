package org.stepic.droid.ui.fragments

import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator
import org.stepic.droid.R
import org.stepic.droid.model.Option
import org.stepic.droid.ui.adapters.SortingStepDraggableAdapter
import org.stepik.android.model.learning.attempts.Attempt
import org.stepik.android.model.learning.replies.Reply

class SortingStepFragment: DraggableStepFragment() {

    override fun getItemDecoration(): RecyclerView.ItemDecoration =
            SimpleListDividerDecorator(ContextCompat.getDrawable(context, R.drawable.list_divider_h), true)

    override fun initLayoutManager(): RecyclerView.LayoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

    override fun initAdapter(): RecyclerView.Adapter<*> = SortingStepDraggableAdapter(optionList)

    override fun initOptionListFromAttempt(attempt: Attempt) {
        optionList = attempt.getDataset()?.options?.mapIndexed { i, option -> Option(option, i) } ?: return
    }

    override fun generateReply(): Reply =
            if (optionList == null)
                Reply()
            else
                Reply(ordering = optionList.map { it.positionId })

    override fun onRestoreSubmission() {
        val ordering = submission.reply?.ordering ?: return

        val positionIdToOption = SparseArray<Option>()
        for (option in optionList) {
            positionIdToOption.append(option.positionId, option)
        }

        optionList.clear()
        optionList.addAll(ordering.map { positionIdToOption[it] })

        wrappedAdapter.notifyDataSetChanged()
    }
}