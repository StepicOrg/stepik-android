package org.stepic.droid.ui.util;

import android.graphics.Rect;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class GridSpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public GridSpacesItemDecoration(int space) {
        this.space = space;
    }

    public void GetItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildLayoutPosition(view);

        /// Only for GridLayoutManager Layouts
        GridLayoutManager manager = (GridLayoutManager) parent.getLayoutManager();

        if (parent.getChildLayoutPosition(view) < manager.getSpanCount())
            outRect.top = space;

        if (position % 2 != 0) {
            outRect.right = space;
        }

        outRect.left = space;
        outRect.bottom = space;
    }
}