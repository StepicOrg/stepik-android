package org.stepic.droid.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.events.InternetIsEnabledEvent;
import org.stepic.droid.events.comments.NewCommentWasAddedOrUpdateEvent;
import org.stepic.droid.events.steps.StepWasUpdatedEvent;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Option;
import org.stepic.droid.model.Pair;
import org.stepic.droid.model.Reply;
import org.stepic.droid.ui.adapters.MatchingStepEnhancedAdapter;
import org.stepic.droid.ui.adapters.SortStepAdapter;
import org.stepic.droid.util.DpPixelsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import timber.log.Timber;

public class MatchingStepFragment extends StepAttemptFragment {

    RecyclerView recyclerView;

    private List<Option> optionList;

    private GridLayoutManager layoutManager;
    private MatchingStepEnhancedAdapter adapter;
    private RecyclerViewDragDropManager recyclerViewDragDropManager;
    private RecyclerView.Adapter wrappedAdapter;

    @Override
    public void onViewCreated(View viewOuter, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(viewOuter, savedInstanceState);
        View view = ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_sorting, attemptContainer, false);
        attemptContainer.addView(view);
        int dp8InPx = (int) DpPixelsHelper.convertDpToPixel(8, getContext());
        attemptContainer.setPadding(0, dp8InPx, 0, dp8InPx);
        recyclerView = ButterKnife.findById(view, R.id.recycler);

        recyclerView.setNestedScrollingEnabled(false);
        layoutManager = new GridLayoutManager(getContext(), 2, LinearLayoutManager.VERTICAL, false);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    protected void showAttempt(Attempt attempt) {
        List<Pair> options = attempt.getDataset().getPairs();
        if (options == null) return;
        optionList = new ArrayList<>(options.size() * 2);
        for (int i = 0; i < options.size(); i++) {
            optionList.add(new Option(options.get(i).getFirst(), i + options.size()));
            optionList.add(new Option(options.get(i).getSecond(), i));
        }

        adapter = new MatchingStepEnhancedAdapter(optionList);
        releaseDragFeature();

        recyclerViewDragDropManager = new RecyclerViewDragDropManager();
        recyclerViewDragDropManager.attachRecyclerView(recyclerView);
        wrappedAdapter = recyclerViewDragDropManager.createWrappedAdapter(adapter);
        recyclerViewDragDropManager.setInitiateOnMove(false);
        recyclerViewDragDropManager.setInitiateOnTouch(true);
        recyclerViewDragDropManager.setOnItemDragEventListener(new RecyclerViewDragDropManager.OnItemDragEventListener() {
            @Override
            public void onItemDragStarted(int position) {
                Timber.d("start drag");
                recyclerView.setNestedScrollingEnabled(true);
            }

            @Override
            public void onItemDragPositionChanged(int fromPosition, int toPosition) {

            }

            @Override
            public void onItemDragFinished(int fromPosition, int toPosition, boolean result) {
                Timber.d("finish drag");
                recyclerView.setNestedScrollingEnabled(false);
            }

            @Override
            public void onItemDragMoveDistanceUpdated(int offsetX, int offsetY) {

            }
        });
        recyclerViewDragDropManager.setItemMoveMode(RecyclerViewDragDropManager.ITEM_MOVE_MODE_SWAP);
        recyclerViewDragDropManager.setCheckCanDropEnabled(true);
        recyclerView.setAdapter(wrappedAdapter);

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

    private void releaseDragFeature() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager.release();
            recyclerViewDragDropManager = null;
        }
        if (wrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(wrappedAdapter);
            wrappedAdapter = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager.cancelDrag();
        }
    }
}
