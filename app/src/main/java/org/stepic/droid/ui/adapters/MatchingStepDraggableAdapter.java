package org.stepic.droid.ui.adapters;

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import org.stepic.droid.R;
import org.stepic.droid.model.Option;

import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class MatchingStepDraggableAdapter extends SortingStepDraggableAdapter {

    private static final int NOT_DRAGGABLE_VIEW_TYPE = 1;

    private int deviceHeightPx;
    private int doublePadding;

    public MatchingStepDraggableAdapter(Activity context, List<Option> data) {
        super(data);

        Display display = context.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        deviceHeightPx = size.y;

        doublePadding = (int) context.getResources().getDimension(R.dimen.half_padding) * 2;
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
                int localHeight = optionViewHolder.enhancedText.getMeasuredHeightOfInnerLayout() + doublePadding;
                Timber.d("localHeight = %s, of view = %s", localHeight, optionViewHolder.container);
                if (localHeight > doublePadding && localHeight < deviceHeightPx) {
                    if (optionViewHolder.sortController != null) {
                        optionViewHolder.sortController.setPadding(sortingControllerPadding, sortingControllerPadding, sortingControllerPadding, sortingControllerPadding);
                    }
                    if (optionViewHolder.sortImageView != null) {
                        optionViewHolder.sortImageView.getLayoutParams().height = sortingImageViewHeight;
                    }
                    optionViewHolder.container.getLayoutParams().height = Math.max(localHeight, minEnhancedTextHeight);
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
