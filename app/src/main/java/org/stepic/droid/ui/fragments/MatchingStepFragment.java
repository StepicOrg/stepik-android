package org.stepic.droid.ui.fragments;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;

import org.stepik.android.model.learning.attempts.Attempt;
import org.stepik.android.model.learning.attempts.Pair;
import org.stepic.droid.model.Option;
import org.stepic.droid.model.Reply;
import org.stepic.droid.ui.adapters.MatchingStepDraggableAdapter;
import org.stepic.droid.ui.util.SimpleDividerItemDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MatchingStepFragment extends DraggableStepFragment {

    @Override
    protected RecyclerView.ItemDecoration getItemDecoration() {
        return new SimpleDividerItemDecoration(getContext());
    }

    @Override
    protected RecyclerView.LayoutManager initLayoutManager() {
        return new GridLayoutManager(getContext(), 2, LinearLayoutManager.VERTICAL, false);
    }

    @Override
    protected RecyclerView.Adapter initAdapter() {
        return new MatchingStepDraggableAdapter(getActivity(), optionList);
    }

    @Override
    protected void initOptionListFromAttempt(Attempt attempt) {
        final List<Pair> options = attempt.getDataset().getPairs();
        if (options != null && options.size() >= 2) {
            optionList = new ArrayList<>(options.size() * 2);
            for (int i = 0; i < options.size(); i++) {
                optionList.add(new Option(options.get(i).getFirst(), i + options.size()));
                optionList.add(new Option(options.get(i).getSecond(), i));
            }
        }
    }

    @Override
    protected void initDragDropManager() {
        super.initDragDropManager();
        recyclerViewDragDropManager.setItemMoveMode(RecyclerViewDragDropManager.ITEM_MOVE_MODE_SWAP);
        recyclerViewDragDropManager.setCheckCanDropEnabled(true);
    }

    @Override
    protected Reply generateReply() {
        if (optionList == null || optionList.size() < 2) return new Reply.Builder().build();

        List<Integer> ordering = new ArrayList<>(optionList.size() / 2);
        for (int i = 1; i < optionList.size(); i += 2) {
            ordering.add(optionList.get(i).getPositionId());
        }

        return new Reply.Builder()
                .setOrdering(ordering)
                .build();
    }

    @Override
    protected void onRestoreSubmission() {
        Reply reply = submission.getReply();
        if (reply == null) return;

        List<Integer> ordering = reply.getOrdering();
        if (ordering == null) return;

        HashMap<Integer, Option> hashMap = new HashMap<>();
        for (Option option : optionList) {
            hashMap.put(option.getPositionId(), option);
        }
        ArrayList<Option> firstColumn = new ArrayList<>(ordering.size() / 2);
        for (int i = 0; i < optionList.size(); i += 2) {
            firstColumn.add(optionList.get(i));
        }

        optionList.clear();
        for (int i = 0; i < ordering.size() * 2; i++) {
            boolean isFirstColumn = i % 2 == 0;
            if (isFirstColumn) {
                int firstColumnIndex = i / 2;
                optionList.add(firstColumn.get(firstColumnIndex));
            } else {
                int orderingIndex = (i - 1) / 2;
                optionList.add(hashMap.get(ordering.get(orderingIndex)));
            }
        }

        wrappedAdapter.notifyDataSetChanged();
    }

}
