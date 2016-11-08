package org.stepic.droid.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.events.InternetIsEnabledEvent;
import org.stepic.droid.events.comments.NewCommentWasAddedOrUpdateEvent;
import org.stepic.droid.events.steps.StepWasUpdatedEvent;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Option;
import org.stepic.droid.model.Reply;
import org.stepic.droid.ui.adapters.SortStepAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;

public class SortingStepFragment extends StepAttemptFragment {

    RecyclerView recyclerView;

    private List<Option> optionList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        View view = ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_sorting, attemptContainer, false);
        attemptContainer.addView(view);
        recyclerView = ButterKnife.findById(view, R.id.recycler);

        recyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setSmoothScrollbarEnabled(true);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    protected void showAttempt(Attempt attempt) {
        List<String> options = attempt.getDataset().getOptions();
        optionList = new ArrayList<>(options.size());
        for (int i = 0; i < options.size(); i++) {
            optionList.add(new Option(options.get(i), i));
        }
        recyclerView.setAdapter(new SortStepAdapter(recyclerView, optionList));
        recyclerView.getAdapter().notifyDataSetChanged();
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
    protected void blockUIBeforeSubmit(boolean needBlock) {
        recyclerView.setEnabled(!needBlock);

    }

    @Override
    protected void onRestoreSubmission() {
        Reply reply = submission.getReply();
        if (reply == null) return;

        List<Integer> ordering = reply.getOrdering();
        if (ordering == null) return;

        SortStepAdapter adapter;
        try {
            adapter = (SortStepAdapter) recyclerView.getAdapter();
        } catch (Exception e) {
            return;
        }


        optionList = adapter.getData();
        optionList.clear();
        Map<Integer, Option> itemIdToOption = adapter.getItemIdOptionMap();
        int i = 0;
        for (Integer itemId : ordering) {
            optionList.add(i, itemIdToOption.get(itemId));
            i++;
        }
        adapter.notifyDataSetChanged();
    }


    @Subscribe
    @Override
    public void onInternetEnabled(InternetIsEnabledEvent enabledEvent) {
        super.onInternetEnabled(enabledEvent);
    }

    @Subscribe
    public void onNewCommentWasAdded(NewCommentWasAddedOrUpdateEvent event) {
        super.onNewCommentWasAdded(event);

    }

    @Subscribe
    public void onStepWasUpdated(StepWasUpdatedEvent event) {
        super.onStepWasUpdated(event);
    }
}
