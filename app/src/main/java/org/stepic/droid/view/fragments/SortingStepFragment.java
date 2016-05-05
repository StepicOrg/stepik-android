package org.stepic.droid.view.fragments;

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
import org.stepic.droid.events.attempts.FailAttemptEvent;
import org.stepic.droid.events.attempts.SuccessAttemptEvent;
import org.stepic.droid.events.submissions.FailGettingLastSubmissionEvent;
import org.stepic.droid.events.submissions.FailSubmissionCreatedEvent;
import org.stepic.droid.events.submissions.SubmissionCreatedEvent;
import org.stepic.droid.events.submissions.SuccessGettingLastSubmissionEvent;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Option;
import org.stepic.droid.model.Reply;
import org.stepic.droid.view.adapters.SortStepAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;

public class SortingStepFragment extends StepWithAttemptsFragment {

    RecyclerView mRecyclerView;

    private List<Option> mOptionList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        View view = ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_sorting, mAttemptContainer, false);
        mAttemptContainer.addView(view);
        mRecyclerView = ButterKnife.findById(view, R.id.recycler);

        mRecyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setSmoothScrollbarEnabled(true);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        return v;
    }

    @Override
    protected void showAttempt(Attempt attempt) {
        List<String> options = attempt.getDataset().getOptions();
        mOptionList = new ArrayList<>(options.size());
        for (int i = 0; i < options.size(); i++) {
            mOptionList.add(new Option(options.get(i), i));
        }
        mRecyclerView.setAdapter(new SortStepAdapter(mRecyclerView, mOptionList));
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    protected Reply generateReply() {
        if (mOptionList == null) return new Reply.Builder().build();

        List<Integer> ordering = new ArrayList<>(mOptionList.size());
        for (int i = 0; i < mOptionList.size(); i++) {
            ordering.add(i, mOptionList.get(i).getPositionId());
        }

        return new Reply.Builder()
                .setOrdering(ordering)
                .build();
    }

    @Override
    protected void blockUIBeforeSubmit(boolean needBlock) {
        mRecyclerView.setEnabled(!needBlock);

    }

    @Override
    protected void onRestoreSubmission() {
        Reply reply = mSubmission.getReply();
        if (reply == null) return;

        List<Integer> ordering = reply.getOrdering();
        if (ordering == null) return;

        SortStepAdapter adapter;
        try {
            adapter = (SortStepAdapter) mRecyclerView.getAdapter();
        } catch (Exception e) {
            return;
        }


        mOptionList = adapter.getData();
        mOptionList.clear();
        Map<Integer, Option> itemIdToOption = adapter.getItemIdOptionMap();
        int i = 0;
        for (Integer itemId : ordering) {
            mOptionList.add(i, itemIdToOption.get(itemId));
            i++;
        }
        adapter.notifyDataSetChanged();
    }


    @Subscribe
    @Override
    public void onInternetEnabled(InternetIsEnabledEvent enabledEvent) {
        super.onInternetEnabled(enabledEvent);
    }

    @Override
    @Subscribe
    public void onSuccessLoadAttempt(SuccessAttemptEvent e) {
        super.onSuccessLoadAttempt(e);
    }

    @Override
    @Subscribe
    public void onSuccessCreateSubmission(SubmissionCreatedEvent e) {
        super.onSuccessCreateSubmission(e);
    }

    @Override
    @Subscribe
    public void onGettingSubmission(SuccessGettingLastSubmissionEvent e) {
        super.onGettingSubmission(e);
    }

    @Subscribe
    @Override
    public void onFailCreateAttemptEvent(FailAttemptEvent event) {
        super.onFailCreateAttemptEvent(event);
    }

    @Subscribe
    @Override
    public void onFailCreateSubmission(FailSubmissionCreatedEvent event) {
        super.onFailCreateSubmission(event);
    }

    @Subscribe
    public void onFailGettingSubmission(FailGettingLastSubmissionEvent e) {
        super.onFailGettingSubmission(e);
    }
}
