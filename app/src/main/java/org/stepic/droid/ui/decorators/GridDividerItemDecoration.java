package org.stepic.droid.ui.decorators;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.stepic.droid.R;

public class GridDividerItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable horizontalDivider;
    private Drawable verticalDivider;

    public GridDividerItemDecoration(Context context) {
        horizontalDivider = ContextCompat.getDrawable(context, R.drawable.bg_divider_vertical);
        verticalDivider = ContextCompat.getDrawable(context, R.drawable.list_divider_w);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int bottom = child.getBottom() + params.bottomMargin;
            int top = bottom - horizontalDivider.getIntrinsicHeight();
            int left = child.getLeft();
            int right = child.getRight();

            horizontalDivider.setBounds(left, top, right, bottom);
            horizontalDivider.draw(c);

            int leftVertical = Math.max(0, right - verticalDivider.getIntrinsicWidth());
            int topVertical = child.getTop();

            verticalDivider.setBounds(leftVertical, topVertical, right, bottom);
            verticalDivider.draw(c);
        }
    }
}