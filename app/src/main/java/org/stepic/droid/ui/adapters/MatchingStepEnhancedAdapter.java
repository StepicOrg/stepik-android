package org.stepic.droid.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.Option;

import java.util.Collections;
import java.util.List;

public class MatchingStepEnhancedAdapter extends SortingStepEnhancedAdapter {

    protected static final int NOT_DRAGGABLE_VIEW_TYPE = 1;

    int optionHeightPx;

    public MatchingStepEnhancedAdapter(List<Option> data) {
        super(data);
        optionHeightPx = (int) MainApplication.getAppContext().getResources().getDimension(R.dimen.option_height);
    }

    @Override
    public OptionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final OptionViewHolder optionViewHolder;
        if (viewType == NOT_DRAGGABLE_VIEW_TYPE) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.view_matching_first_option_enhanced, parent, false);
            optionViewHolder = new OptionViewHolder(view);
        } else if (viewType == DEFAULT_DRAGGABLE_VIEW_TYPE) {
            optionViewHolder = super.onCreateViewHolder(parent, viewType);
        } else {
            throw new IllegalArgumentException("Illegal view type of matching adapter");
        }

        optionViewHolder.container.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                int localHeight = optionViewHolder.container.getMeasuredHeight();
                if (localHeight > optionHeightPx) {
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


    @Override
    public int getItemViewType(int position) {
        if (position % 2 == 0) {
            return NOT_DRAGGABLE_VIEW_TYPE;
        } else {
            return super.getItemViewType(position);
        }
    }
}
