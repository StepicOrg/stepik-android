package org.stepic.droid.ui.adapters;

import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.model.TableChoiceAnswer;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TableQuizAdapter extends RecyclerView.Adapter<TableQuizAdapter.GenericViewHolder> {

    private static final int DESCRIPTION_TYPE = 0;
    private static final int CHECKBOX_TYPE = 1;
    private static final int RADIO_BUTTON_TYPE = 2;
    private static final int ROW_HEADER_TYPE = 3;
    private static final int COLUMN_HEADER_TYPE = 4;


    private final List<String> columns;
    private final List<String> rows;
    private final String description;
    private final boolean isCheckbox;
    private final List<TableChoiceAnswer> answers;

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return DESCRIPTION_TYPE;
        } else if (position <= rows.size()) {
            return ROW_HEADER_TYPE;
        } else if (position % (rows.size() + 1) == 0) {
            return COLUMN_HEADER_TYPE;
        } else if (isCheckbox) {
            return CHECKBOX_TYPE;
        } else {
            return RADIO_BUTTON_TYPE;
        }
    }

    public TableQuizAdapter(List<String> rows, List<String> columns, String description, boolean isCheckbox, List<TableChoiceAnswer> answers) {
        this.columns = columns;
        this.rows = rows;
        this.description = description;
        this.isCheckbox = isCheckbox;
        this.answers = answers;
    }


    @Override
    public GenericViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == RADIO_BUTTON_TYPE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_table_quiz_radio_button_cell, parent, false);
            return new RadioButtonCellViewHolder(v);
        } else if (viewType == CHECKBOX_TYPE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_table_quiz_checkbox_cell, parent, false);
            return new CheckboxCellViewHolder(v);
        } else if (viewType == DESCRIPTION_TYPE || viewType == ROW_HEADER_TYPE || viewType == COLUMN_HEADER_TYPE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_table_quiz_text_cell, parent, false);
            return new DescriptionViewHolder(v);
        } else {
            throw new IllegalStateException("viewType with index " + viewType + " is not supported in table quiz");
        }
    }

    @Override
    public void onBindViewHolder(GenericViewHolder holder, int position) {
        int itemViewType = getItemViewType(position);
        if (itemViewType == DESCRIPTION_TYPE) {
            holder.setData(description);
        } else if (itemViewType == ROW_HEADER_TYPE) {
            String headerText = rows.get(position - 1);
            holder.setData(headerText);
        } else if (itemViewType == COLUMN_HEADER_TYPE) {
            int columnPosition = position / (rows.size() + 1) - 1; // -1 is description cell at top left cell
            String headerText = columns.get(columnPosition);
            holder.setData(headerText);
        } else {
            holder.setData(false);
        }
    }

    @Override
    public int getItemCount() {
        return rows.size() + columns.size() + 1 + rows.size() * columns.size(); //1 – description top left cell.
    }

    static abstract class GenericViewHolder extends RecyclerView.ViewHolder {

        public GenericViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public abstract void setData(@NotNull String text);

        public abstract void setData(@NotNull Boolean needCheckModel);
    }


    static class DescriptionViewHolder extends GenericViewHolder {

        @BindView(R.id.cell_text)
        TextView textView;

        public DescriptionViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void setData(@NotNull String text) {
            textView.setText(text);
        }

        @Override
        public void setData(@NotNull Boolean needCheck) {
            throw new IllegalStateException("description view can't be without text, check position");
        }

    }

    static class CheckboxCellViewHolder extends GenericViewHolder {

        @BindView(R.id.checkbox_cell)
        AppCompatCheckBox checkBox;

        public CheckboxCellViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void setData(@NotNull String text) {
            throw new IllegalStateException("checkbox view can't be without need checked");
        }

        @Override
        public void setData(@NotNull Boolean needCheck) {
            checkBox.setChecked(needCheck);
        }
    }

    static class RadioButtonCellViewHolder extends GenericViewHolder {

        @BindView(R.id.radio_button_cell)
        AppCompatRadioButton radioButton;

        public RadioButtonCellViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void setData(@NotNull String text) {
            throw new IllegalStateException("radio button view can't be without need checked");
        }

        @Override
        public void setData(@NotNull Boolean needCheck) {
            radioButton.setChecked(needCheck);
        }
    }
}
