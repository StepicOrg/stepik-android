package org.stepic.droid.ui.fragments

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import org.stepic.droid.model.Option
import org.stepic.droid.ui.adapters.MatchingStepDraggableAdapter
import org.stepic.droid.ui.util.SimpleDividerItemDecoration
import org.stepik.android.model.learning.attempts.Attempt
import org.stepik.android.model.learning.replies.Reply
import java.util.ArrayList
import java.util.HashMap

class MatchingStepFragment: DraggableStepFragment() {

    override fun getItemDecoration(): RecyclerView.ItemDecoration =
            SimpleDividerItemDecoration(context)

    override fun initLayoutManager(): RecyclerView.LayoutManager =
            GridLayoutManager(context, 2, LinearLayoutManager.VERTICAL, false)

    override fun initAdapter(): RecyclerView.Adapter<*> =
            MatchingStepDraggableAdapter(activity, optionList)

    override fun initOptionListFromAttempt(attempt: Attempt) {
        val options = attempt.getDataset()?.pairs ?: return
        if (options.size >= 2) {
            optionList = ArrayList(options.size * 2)
            for (i in options.indices) {
                optionList.add(Option(options[i].first ?: "", i + options.size))
                optionList.add(Option(options[i].second ?: "", i))
            }
        }
    }

    override fun initDragDropManager() {
        super.initDragDropManager()
        recyclerViewDragDropManager.itemMoveMode = RecyclerViewDragDropManager.ITEM_MOVE_MODE_SWAP
        recyclerViewDragDropManager.isCheckCanDropEnabled = true
    }

    override fun generateReply(): Reply {
        if (optionList == null || optionList.size < 2) return Reply()

        val ordering = ArrayList<Int>(optionList.size / 2)
        for (i in 1 until optionList.size step 2) {
            ordering.add(optionList[i].positionId)
        }

        return Reply(ordering = ordering)
    }

    override fun onRestoreSubmission() {
        val ordering = submission.reply?.ordering ?: return

        val hashMap = SparseArray<Option>()
        for (option in optionList) {
            hashMap.append(option.positionId, option)
        }
        val firstColumn = ArrayList<Option>(ordering.size / 2)

        for (i in 0 until optionList.size step 2) {
            firstColumn.add(optionList[i])
        }

        optionList.clear()
        for (i in 0 until ordering.size * 2) {
            val isFirstColumn = i % 2 == 0
            if (isFirstColumn) {
                val firstColumnIndex = i / 2
                optionList.add(firstColumn[firstColumnIndex])
            } else {
                val orderingIndex = (i - 1) / 2
                optionList.add(hashMap[ordering[orderingIndex]])
            }
        }

        wrappedAdapter.notifyDataSetChanged()
    }
}