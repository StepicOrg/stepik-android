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
import org.stepic.droid.events.comments.NewCommentWasAddedOrUpdateEvent;
import org.stepic.droid.events.steps.StepWasUpdatedEvent;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Option;
import org.stepic.droid.model.Pair;
import org.stepic.droid.model.Reply;
import org.stepic.droid.ui.adapters.SortStepAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;

public class MatchingStepFragment extends StepAttemptFragment {

    RecyclerView recyclerView;
    LinearLayout leftLinearLayout;

    private List<Option> optionList;
    private List<String> firstList;
    private int maxWidth;
    private int halfScreen;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        View view = ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_matching, attemptContainer, false);
        attemptContainer.addView(view);
        recyclerView = ButterKnife.findById(view, R.id.recycler);
        leftLinearLayout = ButterKnife.findById(view, R.id.leftColumn);

        recyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setSmoothScrollbarEnabled(true);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

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
        optionList = new ArrayList<>(options.size());
        firstList = new ArrayList<>(options.size());
        for (int i = 0; i < options.size(); i++) {
            optionList.add(new Option(options.get(i).getSecond(), i));
            firstList.add(options.get(i).getFirst());
        }
        maxWidth = getMaxWidthOfLines();

        buildFirstColumn(firstList);
        recyclerView.setAdapter(new SortStepAdapter(recyclerView, optionList, maxWidth, true));
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    // TODO: 25.01.16 refactor
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

    // TODO: 25.01.16 refactor
    @Override
    protected void blockUIBeforeSubmit(boolean needBlock) {
//        mAttemptContainer.setEnabled(!needBlock);
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

//        if (mFirstList == null || mFirstList.isEmpty()) return;
//        buildFirstColumn(mFirstList);


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

    private void buildFirstColumn(List<String> firstList) {
        if (firstList == null || firstList.isEmpty() || maxWidth <= 0) return;
        leftLinearLayout.removeAllViews();
        for (String value : firstList) {
            View view = ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_matching_first_option, leftLinearLayout, false);
            TextView header = ButterKnife.findById(view, R.id.option_text);
            header.setText(value);
            int lines = (maxWidth / halfScreen) + 1;
            int height = (int) MainApplication.getAppContext().getResources().getDimension(R.dimen.option_height);
            height = lines * height;
            view.getLayoutParams().height = height;
            leftLinearLayout.addView(view);
        }
    }


    private int getMaxWidthOfLines() {
        // TODO: 25.01.16 dirty hack, try to find less dirty
        View view = ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_matching_second_option, leftLinearLayout, false);
        final TextView header = ButterKnife.findById(view, R.id.option_text);


        int maxWidth = 0;
        List<String> allTextList = new ArrayList<>(firstList);
        for (Option option : optionList) {
            allTextList.add(option.getValue());
        }
        for (String text : allTextList) {
            header.setText(text);
            header.setVisibility(View.INVISIBLE);
            leftLinearLayout.addView(view);

            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int measuredWidth = view.getMeasuredWidth();
            if (measuredWidth > maxWidth) {
                maxWidth = measuredWidth;
            }
            leftLinearLayout.removeView(view);
        }

        return maxWidth;
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
