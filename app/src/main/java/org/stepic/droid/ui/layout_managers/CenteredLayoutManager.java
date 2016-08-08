package org.stepic.droid.ui.layout_managers;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

public class CenteredLayoutManager extends LinearLayoutManager {
    private int mParentWidth;
    private int mItemWidth;

    public CenteredLayoutManager(Context context, int parentWidth, int itemWidth) {
        super(context, LinearLayoutManager.HORIZONTAL, false);
        mParentWidth = parentWidth;
        mItemWidth = itemWidth;
    }

    @Override
    public int getPaddingLeft() {
        int count = getChildCount();
        return Math.round(mParentWidth / 2f - mItemWidth * count / 2f);
    }

    @Override
    public int getPaddingRight() {
        return getPaddingLeft();
    }
}