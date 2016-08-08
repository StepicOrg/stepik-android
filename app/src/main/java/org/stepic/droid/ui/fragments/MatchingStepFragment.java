package org.stepic.droid.ui.fragments;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.events.InternetIsEnabledEvent;
import org.stepic.droid.events.attempts.FailAttemptEvent;
import org.stepic.droid.events.attempts.SuccessAttemptEvent;
import org.stepic.droid.events.comments.NewCommentWasAddedOrUpdateEvent;
import org.stepic.droid.events.steps.StepWasUpdatedEvent;
import org.stepic.droid.events.submissions.FailGettingLastSubmissionEvent;
import org.stepic.droid.events.submissions.FailSubmissionCreatedEvent;
import org.stepic.droid.events.submissions.SubmissionCreatedEvent;
import org.stepic.droid.events.submissions.SuccessGettingLastSubmissionEvent;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Option;
import org.stepic.droid.model.Pair;
import org.stepic.droid.model.Reply;
import org.stepic.droid.util.HtmlHelper;
import org.stepic.droid.ui.adapters.SortStepAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;

public class MatchingStepFragment extends StepWithAttemptsFragment {

    RecyclerView mRecyclerView;
    LinearLayout mLeftLinearLayout;

    private List<Option> mOptionList;
    private List<String> mFirstList;
    private int mMaxWidth;
    private int halfScreen;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        View view = ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_matching, mAttemptContainer, false);
        mAttemptContainer.addView(view);
        mRecyclerView = ButterKnife.findById(view, R.id.recycler);
        mLeftLinearLayout = ButterKnife.findById(view, R.id.leftColumn);

        mRecyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setSmoothScrollbarEnabled(true);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        WindowManager wm = (WindowManager) MainApplication.getAppContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        halfScreen = screenWidth / 2;

        return v;
    }

    @Override
    protected void showAttempt(Attempt attempt) {
        List<Pair> options = attempt.getDataset().getPairs();
        if (options == null) return;
        mOptionList = new ArrayList<>(options.size());
        mFirstList = new ArrayList<>(options.size());
        for (int i = 0; i < options.size(); i++) {
            mOptionList.add(new Option(options.get(i).getSecond(), i));
            mFirstList.add(options.get(i).getFirst());
        }
        mMaxWidth = getMaxWidthOfLines();

        buildFirstColumn(mFirstList);
        mRecyclerView.setAdapter(new SortStepAdapter(mRecyclerView, mOptionList, mMaxWidth, true));
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    // TODO: 25.01.16 refactor
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

    // TODO: 25.01.16 refactor
    @Override
    protected void blockUIBeforeSubmit(boolean needBlock) {
//        mAttemptContainer.setEnabled(!needBlock);
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

//        if (mFirstList == null || mFirstList.isEmpty()) return;
//        buildFirstColumn(mFirstList);


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

    private void buildFirstColumn(List<String> firstList) {
        if (firstList == null || firstList.isEmpty() || mMaxWidth <= 0) return;
        mLeftLinearLayout.removeAllViews();
        for (String value : firstList) {
            View view = ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_matching_first_option, mLeftLinearLayout, false);
            TextView header = ButterKnife.findById(view, R.id.option_text);
            header.setText(HtmlHelper.fromHtml(value).toString());
            int lines = (mMaxWidth / halfScreen) + 1;
            int height = (int) MainApplication.getAppContext().getResources().getDimension(R.dimen.option_height);
            height = lines * height;
            view.getLayoutParams().height = height;
            mLeftLinearLayout.addView(view);
        }
    }


    private int getMaxWidthOfLines() {
        // TODO: 25.01.16 dirty hack, try to find less dirty
        View view = ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_matching_second_option, mLeftLinearLayout, false);
        final TextView header = ButterKnife.findById(view, R.id.option_text);


        int maxWidth = 0;
        List<String> allTextList = new ArrayList<>(mFirstList);
        for (Option option : mOptionList) {
            allTextList.add(option.getValue());
        }
        for (String text : allTextList) {
            header.setText(HtmlHelper.fromHtml(text));
            header.setVisibility(View.INVISIBLE);
            mLeftLinearLayout.addView(view);

            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int measuredWidth = view.getMeasuredWidth();
            if (measuredWidth > maxWidth) {
                maxWidth = measuredWidth;
            }
            mLeftLinearLayout.removeView(view);
        }

        return maxWidth;
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

    @Subscribe
    public void onNewCommentWasAdded(NewCommentWasAddedOrUpdateEvent event) {
        super.onNewCommentWasAdded(event);

    }

    @Subscribe
    public void onStepWasUpdated(StepWasUpdatedEvent event) {
        super.onStepWasUpdated(event);
    }
}
