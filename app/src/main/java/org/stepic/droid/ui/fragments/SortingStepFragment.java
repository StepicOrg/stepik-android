package org.stepic.droid.ui.fragments;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;

import org.stepic.droid.R;
import org.stepik.android.model.learning.attempts.Attempt;
import org.stepic.droid.model.Option;
import org.stepic.droid.model.Reply;
import org.stepic.droid.ui.adapters.SortingStepDraggableAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SortingStepFragment extends DraggableStepFragment {

    @Override
    protected RecyclerView.ItemDecoration getItemDecoration() {
        return new SimpleListDividerDecorator(ContextCompat.getDrawable(getContext(), R.drawable.list_divider_h), true);
    }

    @Override
    protected RecyclerView.LayoutManager initLayoutManager() {
        return new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
    }

    @Override
    protected RecyclerView.Adapter initAdapter() {
        return new SortingStepDraggableAdapter(optionList);
    }

    @Override
    protected void initOptionListFromAttempt(Attempt attempt) {
        List<String> options = attempt.getDataset().getOptions();
        optionList = new ArrayList<>(options.size());
        for (int i = 0; i < options.size(); i++) {
            optionList.add(new Option(options.get(i), i));
        }
    }

    @Override
    protected Reply generateReply() {
        if (optionList == null) return new Reply.Builder().build();

        List<Integer> ordering = new ArrayList<>(optionList.size());
        for (int i = 0; i < optionList.size(); i++) {
            ordering.add(i, optionList.get(i).getPositionId());
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
        optionList.clear();
        int i = 0;
        for (Integer itemId : ordering) {
            optionList.add(i, hashMap.get(itemId));
            i++;
        }

        wrappedAdapter.notifyDataSetChanged();
    }

}
