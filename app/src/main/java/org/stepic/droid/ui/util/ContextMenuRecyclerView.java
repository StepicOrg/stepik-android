package org.stepic.droid.ui.util;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;

public class ContextMenuRecyclerView extends RecyclerView {

    private RecyclerViewContextMenuInfo contextMenuInfo;

    public ContextMenuRecyclerView(Context context) {
        this(context, null);

    }

    public ContextMenuRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContextMenuRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {

    }

    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return contextMenuInfo;
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {
        int longPressPosition;
        try {
            longPressPosition = getChildAdapterPosition(originalView);
        } catch (Exception ex) {
//            if cannot cast or another exception --> do not show menu
            return false;
        }

        if (longPressPosition >= 0) {
            final long longPressId = getAdapter().getItemId(longPressPosition);
            contextMenuInfo = new RecyclerViewContextMenuInfo(longPressPosition, longPressId);
            return super.showContextMenuForChild(originalView);
        }
        return false;
    }

    public static class RecyclerViewContextMenuInfo implements ContextMenu.ContextMenuInfo {

        public RecyclerViewContextMenuInfo(int position, long id) {
            this.position = position;
            this.id = id;
        }

        final public int position;
        final public long id;
    }
}