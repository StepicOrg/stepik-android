package org.stepic.droid.ui.adapters;

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
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.model.Option;
import org.stepic.droid.ui.custom.LatexSupportableEnhancedFrameLayout;
import org.stepic.droid.ui.custom.dragsortadapter.DragSortAdapter;
import org.stepic.droid.ui.custom.dragsortadapter.NoForegroundShadowBuilder;
import org.stepic.droid.util.resolvers.text.TextResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SortStepAdapter extends DragSortAdapter<SortStepAdapter.OptionViewHolder> {

    private final List<Option> data;
    private final int width;
    private final int halfScreen;
    private final Map<Integer, Option> itemIdOptionMap;
    private final boolean isMatching;

    @Inject
    TextResolver textResolver;

    public SortStepAdapter(RecyclerView recyclerView, List<Option> data, int width, boolean isMatching) {
        super(recyclerView);
        MainApplication.component().inject(this);
        this.data = data;
        this.width = width;
        itemIdOptionMap = new HashMap<>();
        for (Option option : data) {
            itemIdOptionMap.put(option.getPositionId(), option);
        }

        WindowManager wm = (WindowManager) MainApplication.getAppContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        halfScreen = screenWidth / 2;
        this.isMatching = isMatching;
    }


    public SortStepAdapter(RecyclerView recyclerView, List<Option> data, int width) {
        this(recyclerView, data, width, false);
    }

    public SortStepAdapter(RecyclerView recyclerView, List<Option> data) {
        this(recyclerView, data, 0);
    }

    public List<Option> getData() {
        return data;
    }

    public Map<Integer, Option> getItemIdOptionMap() {
        return itemIdOptionMap;
    }

    @Override
    public OptionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        if (isMatching) {
            view = inflater.inflate(R.layout.view_matching_second_option, parent, false);
        } else {
            view = inflater.inflate(R.layout.view_sorting_option, parent, false);
        }
        OptionViewHolder holder = new OptionViewHolder(this, view, isMatching);
        view.setOnClickListener(holder);
        view.setOnLongClickListener(holder);
        view.setOnTouchListener(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(OptionViewHolder holder, int position) {
        int itemId = data.get(position).getPositionId();
        if (width > 0) {
            int lines = (width / halfScreen) + 1;
            int height = (int) MainApplication.getAppContext().getResources().getDimension(R.dimen.option_height);
            height = lines * height;
            holder.container.getLayoutParams().height = height;
//            holder.mOptionText.setLines((mWidth / halfScreen) + 1);
        }

        if (!isMatching) {
            holder.enhancedText.setText(itemIdOptionMap.get(itemId).getValue());
        } else {
            holder.optionText.setText(itemIdOptionMap.get(itemId).getValue());
        }
        // NOTE: check for getDraggingId() match to set an "invisible space" while dragging
        holder.container.setVisibility(getDraggingId() == itemId ? View.INVISIBLE : View.VISIBLE);
        holder.container.postInvalidate();
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
        Option option = itemIdOptionMap.get(id_int);
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
            View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {

        private final boolean isMatching;

        @BindView(R.id.container)
        ViewGroup container;

        TextView optionText;

        @BindView(R.id.sort_icon)
        View sortController;

        LatexSupportableEnhancedFrameLayout enhancedText;


        public OptionViewHolder(DragSortAdapter adapter, View itemView, boolean isMatching) {
            super(adapter, itemView);
            this.isMatching = isMatching;
            ButterKnife.bind(this, itemView);
            //// FIXME: 26.04.16 refactor this
            if (this.isMatching) {
                optionText = (TextView) itemView.findViewById(R.id.option_text);
            } else {
                enhancedText = (LatexSupportableEnhancedFrameLayout) itemView.findViewById(R.id.option_text);
            }
            sortController.setOnTouchListener(new View.OnTouchListener() {
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
            if (isMatching) {
                startDrag();
            }
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

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            return false;
        }
    }
}
