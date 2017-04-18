package org.stepic.droid.ui.adapters;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;

import org.stepic.droid.R;
import org.stepic.droid.base.App;
import org.stepic.droid.model.Option;
import org.stepic.droid.ui.custom.ProgressLatexView;
import org.stepic.droid.ui.util.ViewUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SortingStepDraggableAdapter extends RecyclerView.Adapter<SortingStepDraggableAdapter.OptionViewHolder>
        implements DraggableItemAdapter<SortingStepDraggableAdapter.OptionViewHolder> {

    @Nullable
    private RecyclerView recyclerView;
    protected final static int DEFAULT_DRAGGABLE_VIEW_TYPE = 0;
    protected final List<Option> data;
    protected final int sortingImageViewHeight;
    protected final int sortingControllerPadding;
    protected final int minEnhancedTextHeight;

    public SortingStepDraggableAdapter(List<Option> data) {
        super();
        this.data = data;
        setHasStableIds(true);

        sortingImageViewHeight = (int) App.Companion.getAppContext().getResources().getDimension(R.dimen.sort_image_view_height);
        sortingControllerPadding = (int) App.Companion.getAppContext().getResources().getDimension(R.dimen.padding_sort_controller);
        minEnhancedTextHeight = sortingImageViewHeight + 2 * sortingControllerPadding;
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getPositionId();
    }

    @Override
    public OptionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.view_draggable_option, parent, false);
        OptionViewHolder optionViewHolder = new OptionViewHolder(view);

        optionViewHolder.sortImageView.getLayoutParams().height = sortingImageViewHeight;

        optionViewHolder.sortController.setPadding(sortingControllerPadding, sortingControllerPadding, sortingControllerPadding, sortingControllerPadding);

        optionViewHolder.enhancedText.setMinimumHeight(minEnhancedTextHeight);
        return optionViewHolder;
    }

    @Override
    public void onBindViewHolder(OptionViewHolder holder, int position) {
        String text = data.get(position).getValue();
        holder.enhancedText.setPlainOrLaTeXText(text);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public boolean onCheckCanStartDrag(OptionViewHolder holder, int position, int x, int y) {
        return recyclerView != null && recyclerView.isEnabled() && ViewUtils.hitTest(holder.sortController, x, y);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(OptionViewHolder holder, int position) {
        // no drag-sortable range specified
        return null;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }
        data.add(toPosition, data.remove(fromPosition));
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return true;
    }

    static class OptionViewHolder extends AbstractDraggableItemViewHolder {

        @BindView(R.id.container)
        View container;

        @Nullable
        @BindView(R.id.sort_icon)
        View sortController;

        @BindView(R.id.option_text)
        ProgressLatexView enhancedText;

        @Nullable
        @BindView(R.id.sort_image_view)
        View sortImageView;

        OptionViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }
}
