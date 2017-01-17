package org.stepic.droid.ui.adapters;

import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import org.stepic.droid.model.Option;

import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class MatchingStepEnhancedAdapter extends SortingStepEnhancedAdapter {

    public MatchingStepEnhancedAdapter(List<Option> data) {
        super(data);
    }

    @Override
    public OptionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final OptionViewHolder optionViewHolder = super.onCreateViewHolder(parent, viewType);
        optionViewHolder.container.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                int localHeight = optionViewHolder.container.getMeasuredHeight();
                Timber.d("MeasuredHeight = %s", localHeight);
                if (localHeight != 0) {
                    optionViewHolder.container.getLayoutParams().height = localHeight;
                    optionViewHolder.container.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return true;
            }
        });
        return optionViewHolder;
    }

    @Override
    public boolean onCheckCanStartDrag(OptionViewHolder holder, int position, int x, int y) {
        return position % 2 != 0 && super.onCheckCanStartDrag(holder, position, x, y);
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
        return dropPosition % 2 != 0;
    }
}
