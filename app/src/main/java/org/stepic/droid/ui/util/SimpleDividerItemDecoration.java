package org.stepic.droid.ui.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import org.stepic.droid.R;

public class SimpleDividerItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable divider;
    private Drawable verticalDivider;

    public SimpleDividerItemDecoration(Context context) {
        divider = ContextCompat.getDrawable(context, R.drawable.list_divider_h);
        verticalDivider = ContextCompat.getDrawable(context, R.drawable.list_divider_w);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + divider.getIntrinsicHeight();

            divider.setBounds(left, top, right, bottom);
            divider.draw(c);

            int leftVertical = child.getLeft();
            int rightVertical = leftVertical + verticalDivider.getIntrinsicWidth();
            int topVertical = child.getTop();

            verticalDivider.setBounds(leftVertical, topVertical, rightVertical, bottom);
            verticalDivider.draw(c);
        }
    }
}