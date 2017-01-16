package org.stepic.droid.ui.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.events.InternetIsEnabledEvent;
import org.stepic.droid.events.comments.NewCommentWasAddedOrUpdateEvent;
import org.stepic.droid.events.steps.StepWasUpdatedEvent;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Option;
import org.stepic.droid.model.Reply;
import org.stepic.droid.ui.adapters.SortStepAdapter;
import org.stepic.droid.ui.adapters.SortingStepEnhancedAdapter;
import org.stepic.droid.util.DpPixelsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import timber.log.Timber;

public class SortingStepFragment extends StepAttemptFragment {

    RecyclerView recyclerView;

    private List<Option> optionList;

    private RecyclerViewDragDropManager recyclerViewDragDropManager;

    private RecyclerView.Adapter wrappedAdapter;

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        View view = ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_sorting, attemptContainer, false);
        int dp8inPx = (int) DpPixelsHelper.convertDpToPixel(8f);
        attemptContainer.setPadding(0, dp8inPx, 0, dp8inPx);
        attemptContainer.addView(view);
        recyclerView = ButterKnife.findById(view, R.id.recycler);

        recyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setSmoothScrollbarEnabled(true);

        recyclerView.setLayoutManager(layoutManager);

        final GeneralItemAnimator animator = new DraggableItemAnimator();
        recyclerView.setItemAnimator(animator);

        recyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getContext(), R.drawable.list_divider_h), true));
    }

    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    @Override
    protected void showAttempt(Attempt attempt) {
        List<String> options = attempt.getDataset().getOptions();
        optionList = new ArrayList<>(options.size());
        for (int i = 0; i < options.size(); i++) {
            optionList.add(new Option(options.get(i), i));
        }
        SortingStepEnhancedAdapter adapter = new SortingStepEnhancedAdapter(optionList);

        releaseDragFeature();
        // drag & drop manager
        recyclerViewDragDropManager = new RecyclerViewDragDropManager();
//        recyclerViewDragDropManager.setDraggingItemShadowDrawable((NinePatchDrawable) ContextCompat.getDrawable(getContext(), R.drawable.material_shadow_z3)); //if not transparent background
        // Lollipop or later has native drop shadow feature. ItemShadowDecorator is not required.
//        if (!supportsViewElevation()) {
//            recyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) ContextCompat.getDrawable(getContext(), R.drawable.material_shadow_z1))); if not transparent background
//        }
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

        recyclerView.setAdapter(wrappedAdapter);
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

    @Override
    public void onDestroyView() {
        releaseDragFeature();
        super.onDestroyView();
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
        recyclerViewDragDropManager.cancelDrag();
        super.onPause();
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
