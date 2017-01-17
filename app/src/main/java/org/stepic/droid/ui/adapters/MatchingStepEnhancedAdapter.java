package org.stepic.droid.ui.adapters;

import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import org.stepic.droid.model.Option;

import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class MatchingStepEnhancedAdapter extends SortingStepEnhancedAdapter {
    int draggableUpperBound;

    public MatchingStepEnhancedAdapter(List<Option> data) {
        super(data);
        draggableUpperBound = (data.size() / 2) - 1;
    }

//    @Override
//    public ItemDraggableRange onGetItemDraggableRange(OptionViewHolder holder, int position) {
//        return new ItemDraggableRange(0, draggableUpperBound);
//    }


    @Override
    public OptionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final OptionViewHolder optionViewHolder = super.onCreateViewHolder(parent, viewType);
        optionViewHolder.container.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                int localHeight = optionViewHolder.container.getMeasuredHeight();
                int localWidth = optionViewHolder.container.getMeasuredWidth();
                Timber.d("MeasuredHeight = %s", localHeight);
                if (localHeight != 0) {
                    optionViewHolder.container.getLayoutParams().height = localHeight;
                    optionViewHolder.container.getViewTreeObserver().removeOnPreDrawListener(this);
                    return true;
                } else {
                    return false;
                }
            }
        });
        return optionViewHolder;
    }

    @Override
    public boolean onCheckCanStartDrag(OptionViewHolder holder, int position, int x, int y) {
        if (position % 2 == 0) {
            return false;
        } else {
            return super.onCheckCanStartDrag(holder, position, x, y);
        }
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }
        Collections.swap(data, toPosition, fromPosition);
        notifyDataSetChanged();
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        boolean canDrop = dropPosition % 2 != 0;
        return canDrop;
    }
}
