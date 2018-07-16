package org.stepic.droid.ui.adapters;

import android.app.Activity;
import android.graphics.Point;
import android.support.annotation.ColorInt;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepik.android.model.learning.reply.TableChoiceAnswer;
import org.stepic.droid.ui.custom.ProgressLatexView;
import org.stepic.droid.ui.listeners.CheckedChangeListenerWithPosition;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TableChoiceAdapter extends RecyclerView.Adapter<TableChoiceAdapter.GenericViewHolder> implements CheckedChangeListenerWithPosition {

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
    private boolean isAllEnabled = true;

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

    public TableChoiceAdapter(Activity context, List<String> rows, List<String> columns, String description, boolean isCheckbox, List<TableChoiceAnswer> answers) {
        this.columns = columns;
        this.rows = rows;
        this.description = description;
        this.isCheckbox = isCheckbox;
        this.answers = answers;

        Display display = context.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
    }


    @Override
    public GenericViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == RADIO_BUTTON_TYPE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_table_quiz_radio_button_cell, parent, false);
            return new RadioButtonCellViewHolder(v, this);
        } else if (viewType == CHECKBOX_TYPE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_table_quiz_checkbox_cell, parent, false);
            return new CheckboxCellViewHolder(v, this);
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
            // Description is always default (not even or odd)
        } else if (itemViewType == ROW_HEADER_TYPE) {
            String headerText = rows.get(position - 1);
            holder.setData(headerText);
            holder.fillAsEven(isPositionEven(position));
        } else if (itemViewType == COLUMN_HEADER_TYPE) {
            int columnPosition = getColumnPosition(position); // -1 is description cell at top left cell
            String headerText = columns.get(columnPosition);
            holder.setData(headerText);
            //Column header is always default. (not even or odd)
        } else {
            List<TableChoiceAnswer.Cell> oneRowAnswers = getOneRowAnswersFromPosition(position);
            int columnPosition = getColumnPosition(position);
            TableChoiceAnswer.Cell cell = oneRowAnswers.get(columnPosition);
            holder.setData(cell.getAnswer());
            holder.fillAsEven(isPositionEven(position));
        }
        holder.makeEnabled(isAllEnabled);
    }

    private int getColumnPosition(int position) {
        return position / (rows.size() + 1) - 1;
    }

    private List<TableChoiceAnswer.Cell> getOneRowAnswersFromPosition(int position) {
        int rowPosition = (position - 1) % (rows.size() + 1);
        TableChoiceAnswer tableChoiceAnswer = answers.get(rowPosition);
        return tableChoiceAnswer.getColumns();
    }

    @Override
    public int getItemCount() {
        return rows.size() + columns.size() + 1 + rows.size() * columns.size(); //1 – description top left cell.
    }

    @Override
    public void onCheckedChanged(CompoundButton view, boolean isChecked, int position) {
        List<TableChoiceAnswer.Cell> oneRowAnswers = getOneRowAnswersFromPosition(position);
        int columnPosition = getColumnPosition(position);

        int multiplier = rows.size() + 1;
        int remainder = position % multiplier;


        List<Integer> changed = new ArrayList<>();
        if (!isCheckbox && isChecked) {
            //radio button, check something -> uncheck others
            int i = 1;
            for (TableChoiceAnswer.Cell eachCellInRow : oneRowAnswers) {
                if (eachCellInRow.getAnswer()) {
                    //if something is checked
                    int currentAdapterPosition = multiplier * i + remainder;
                    eachCellInRow.setAnswer(false);
                    if (currentAdapterPosition != position) {
                        changed.add(currentAdapterPosition);
                    }
                }
                i++;
            }
        }

        // change checked state
        TableChoiceAnswer.Cell cell = oneRowAnswers.get(columnPosition);
        cell.setAnswer(isChecked);

        if (!changed.isEmpty()) {
            for (Integer changedPosition : changed) {
                notifyItemChanged(changedPosition); // In the perfect world there is only one for radiobutton
            }
        }
    }

    public void setAllItemsEnabled(boolean isAllEnabled) {
        this.isAllEnabled = isAllEnabled;
        int i = rows.size() + 2; // the first option cell element
        while (i < getItemCount()) {
            notifyItemRangeChanged(i, rows.size());
            i += rows.size() + 1;
        }
    }

    /**
     * 0 is header row (even)
     * 1 is the next row (odd)
     * 2 is the next (even)
     * etc
     *
     * @param position of the element
     * @return true if position exists in even row, false otherwise
     */
    private boolean isPositionEven(int position) {
        int rowPosition = position % (rows.size() + 1);
        return rowPosition % 2 == 0;
    }

    static abstract class GenericViewHolder extends RecyclerView.ViewHolder {

        @ColorInt
        @BindColor(R.color.table_even_row)
        protected int backgroundColorForEven;

        @ColorInt
        @BindColor(R.color.white)
        protected int backgroundColorForNotEven;

        GenericViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public abstract void setData(@NotNull String text);

        public abstract void setData(@NotNull Boolean needCheckModel);

        public abstract void makeEnabled(boolean isAllEnabled);

        public abstract void fillAsEven(boolean positionEven);
    }


    static class DescriptionViewHolder extends GenericViewHolder {

        @BindView(R.id.cell_text)
        ProgressLatexView latexView;

        DescriptionViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void setData(@NotNull String text) {
            latexView.setAnyText(text);
        }

        @Override
        public void setData(@NotNull Boolean needCheck) {
            throw new IllegalStateException("description view can't be without text, check position");
        }

        @Override
        public void makeEnabled(boolean isAllEnabled) {
            //do nothing, it is not interactable by user
        }

        @Override
        public void fillAsEven(boolean positionEven) {
            latexView.setBackgroundColor(positionEven ? backgroundColorForEven : backgroundColorForNotEven);
        }

    }


    static abstract class CompoundButtonViewHolder extends GenericViewHolder {

        @BindView(R.id.container)
        ViewGroup container;

        CompoundButtonViewHolder(View itemView, final CheckedChangeListenerWithPosition checkedChangeListenerWithPosition) {
            super(itemView);
            getCheckableView().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (buttonView.isPressed()) {
                        checkedChangeListenerWithPosition.onCheckedChanged(buttonView, isChecked, getAdapterPosition());
                    }
                }
            });

            container.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return getCheckableView().dispatchTouchEvent(event);
                }
            });
        }

        @Override
        public final void setData(@NotNull String text) {
            throw new IllegalStateException("text is not allowed in table view quiz");
        }

        @Override
        public final void setData(@NotNull Boolean needCheckModel) {
            getCheckableView().setChecked(needCheckModel);
        }

        @Override
        public void makeEnabled(boolean isEnabled) {
            container.setClickable(isEnabled);
            getCheckableView().setClickable(isEnabled);
        }

        @Override
        public void fillAsEven(boolean positionEven) {
            container.setBackgroundColor(positionEven ? backgroundColorForEven : backgroundColorForNotEven);
        }

        abstract CompoundButton getCheckableView();
    }

    static class CheckboxCellViewHolder extends CompoundButtonViewHolder {

        @BindView(R.id.checkbox_cell)
        AppCompatCheckBox checkBox;

        CheckboxCellViewHolder(View itemView, CheckedChangeListenerWithPosition checkedChangeListenerWithPosition) {
            super(itemView, checkedChangeListenerWithPosition);
        }

        @Override
        CompoundButton getCheckableView() {
            return checkBox;
        }
    }

    static class RadioButtonCellViewHolder extends CompoundButtonViewHolder {

        @BindView(R.id.radio_button_cell)
        AppCompatRadioButton radioButton;

        RadioButtonCellViewHolder(View itemView, final CheckedChangeListenerWithPosition checkedChangeListenerWithPosition) {
            super(itemView, checkedChangeListenerWithPosition);
        }

        @Override
        CompoundButton getCheckableView() {
            return radioButton;
        }
    }
}
