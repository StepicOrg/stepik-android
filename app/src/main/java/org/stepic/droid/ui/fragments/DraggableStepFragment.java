package org.stepic.droid.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import org.stepic.droid.R;
import org.stepik.android.model.attempts.Attempt;
import org.stepic.droid.model.Option;
import org.stepic.droid.util.DpPixelsHelper;

import java.util.List;

public abstract class DraggableStepFragment extends StepAttemptFragment {

    private RecyclerView recyclerView;
    protected List<Option> optionList;
    protected RecyclerViewDragDropManager recyclerViewDragDropManager;
    protected RecyclerView.Adapter wrappedAdapter;


    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        View draggableContainer = ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_draggable_container, attemptContainer, false);

        int dp8inPx = (int) DpPixelsHelper.convertDpToPixel(8f);
        attemptContainer.setPadding(0, dp8inPx, 0, dp8inPx);
        attemptContainer.addView(draggableContainer);

        recyclerView = draggableContainer.findViewById(R.id.recycler);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(initLayoutManager());
        recyclerView.setItemAnimator(new DraggableItemAnimator());
        recyclerView.addItemDecoration(getItemDecoration());
    }

    @Override
    protected final void showAttempt(Attempt attempt) {
        initOptionListFromAttempt(attempt);
        if (optionList == null || optionList.isEmpty()) {
            return;
        }
        releaseDragFeature();
        initDragDropManager();
        wrappedAdapter.notifyDataSetChanged();
    }


    protected abstract RecyclerView.ItemDecoration getItemDecoration();

    protected abstract RecyclerView.LayoutManager initLayoutManager();

    protected abstract void initOptionListFromAttempt(Attempt attempt);

    protected abstract RecyclerView.Adapter initAdapter();

    @Override
    protected final void blockUIBeforeSubmit(boolean needBlock) {
        recyclerView.setEnabled(!needBlock);
    }

    @Override
    public void onPause() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager.cancelDrag();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        releaseDragFeature();
        super.onDestroyView();
    }

    protected final void releaseDragFeature() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager.release();
            recyclerViewDragDropManager = null;
        }
        if (wrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(wrappedAdapter);
            wrappedAdapter = null;
        }
    }

    @CallSuper
    protected void initDragDropManager() {
        RecyclerView.Adapter adapter = initAdapter();
        recyclerViewDragDropManager = new RecyclerViewDragDropManager();
        recyclerViewDragDropManager.attachRecyclerView(recyclerView);
        wrappedAdapter = recyclerViewDragDropManager.createWrappedAdapter(adapter);
        recyclerViewDragDropManager.setInitiateOnMove(false);
        recyclerViewDragDropManager.setInitiateOnTouch(true);
        recyclerViewDragDropManager.setOnItemDragEventListener(new RecyclerViewDragDropManager.OnItemDragEventListener() {
            @Override
            public void onItemDragStarted(int position) {
                recyclerView.setNestedScrollingEnabled(true);
            }

            @Override
            public void onItemDragPositionChanged(int fromPosition, int toPosition) {

            }

            @Override
            public void onItemDragFinished(int fromPosition, int toPosition, boolean result) {
                recyclerView.setNestedScrollingEnabled(false);
            }

            @Override
            public void onItemDragMoveDistanceUpdated(int offsetX, int offsetY) {

            }
        });
        recyclerView.setAdapter(wrappedAdapter);
    }
}
