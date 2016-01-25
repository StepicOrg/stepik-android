package org.stepic.droid.view.adapters;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.Option;
import org.stepic.droid.view.custom.dragsortadapter.DragSortAdapter;
import org.stepic.droid.view.custom.dragsortadapter.NoForegroundShadowBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SortStepAdapter extends DragSortAdapter<SortStepAdapter.OptionViewHolder> {

    private final List<Option> data;
    private final int mWidth;
    private final int halfScreen;
    private final Map<Integer, Option> mItemIdOptionMap;

    public SortStepAdapter(RecyclerView recyclerView, List<Option> data, int width) {
        super(recyclerView);
        this.data = data;
        mWidth = width;
        mItemIdOptionMap = new HashMap<>();
        for (Option option : data) {
            mItemIdOptionMap.put(option.getPositionId(), option);
        }

        WindowManager wm = (WindowManager) MainApplication.getAppContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        halfScreen = screenWidth / 2;
    }

    public SortStepAdapter(RecyclerView recyclerView, List<Option> data) {
        this(recyclerView, data, 0);
    }

    public List<Option> getData() {
        return data;
    }

    public Map<Integer, Option> getItemIdOptionMap() {
        return mItemIdOptionMap;
    }

    @Override
    public OptionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.view_sorting_option, parent, false);
        OptionViewHolder holder = new OptionViewHolder(this, view);
        view.setOnClickListener(holder);
        view.setOnLongClickListener(holder);
//        view.setOnTouchListener(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(OptionViewHolder holder, int position) {
        int itemId = data.get(position).getPositionId();
        if (mWidth > 0) {
            holder.mOptionText.setLines((mWidth / halfScreen) + 1);
        }
        holder.mOptionText.setText(mItemIdOptionMap.get(itemId).getValue());
        // NOTE: check for getDraggingId() match to set an "invisible space" while dragging
        holder.mContainer.setVisibility(getDraggingId() == itemId ? View.INVISIBLE : View.VISIBLE);
        holder.mContainer.postInvalidate();
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getPositionId();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getPositionForId(long id) {
        int id_int = (int) id;
        Option option = mItemIdOptionMap.get(id_int);
        return data.indexOf(option);
    }

    @Override
    public boolean move(int fromPosition, int toPosition) {
        if (fromPosition < 0 || toPosition < 0 || fromPosition >= data.size() || toPosition > data.size())
            return false;
        data.add(toPosition, data.remove(fromPosition));
        return true;
    }

    public static class OptionViewHolder extends DragSortAdapter.ViewHolder implements
            View.OnClickListener, View.OnLongClickListener {

        @Bind(R.id.container)
        ViewGroup mContainer;
        @Bind(R.id.option_text)
        TextView mOptionText;
        @Bind(R.id.sort_icon)
        ImageView mSortImageView;

        public OptionViewHolder(DragSortAdapter adapter, View itemView) {
            super(adapter, itemView);
            ButterKnife.bind(this, itemView);
            mSortImageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    startDrag();
                    return true;
                }
            });
        }


        @Override
        public void onClick(@NonNull View v) {
//            startDrag();
        }

        @Override
        public boolean onLongClick(@NonNull View v) {
            startDrag();
            return true;
        }

        @Override
        public View.DragShadowBuilder getShadowBuilder(View itemView, Point touchPoint) {
            return new NoForegroundShadowBuilder(itemView, touchPoint);
        }
    }
}
