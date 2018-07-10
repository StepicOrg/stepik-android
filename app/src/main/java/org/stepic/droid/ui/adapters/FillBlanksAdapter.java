package org.stepic.droid.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.stepic.droid.R;
import org.stepic.droid.ui.custom.ProgressLatexView;
import org.stepic.droid.ui.util.NothingSelectedSpinnerAdapter;
import org.stepik.android.model.learning.attempts.FillBlankComponent;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class FillBlanksAdapter extends RecyclerView.Adapter<FillBlanksAdapter.FillBlankViewHolderBase> {
    final static int TEXT_TYPE = 0;
    final static int INPUT_TYPE = 1;
    final static int SELECT_TYPE = 2;

    private List<FillBlankComponent> componentList;
    private boolean isAllEnabled = true;

    public FillBlanksAdapter(@NotNull List<FillBlankComponent> componentList) {
        this.componentList = componentList;
    }

    @Override
    public FillBlankViewHolderBase onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        if (viewType == TEXT_TYPE) {
            View view = layoutInflater.inflate(R.layout.fill_blanks_text_option, parent, false);
            return new TextViewHolder(view);
        } else if (viewType == INPUT_TYPE) {
            View view = layoutInflater.inflate(R.layout.fill_blanks_input_option, parent, false);
            return new InputViewHolder(view);
        } else if (viewType == SELECT_TYPE) {
            View view = layoutInflater.inflate(R.layout.fill_blanks_select_option, parent, false);
            return new SelectViewHolder(view);
        } else {
            throw new IllegalStateException("view type of fill blanks was illegal");
        }
    }

    @Override
    public int getItemViewType(int position) {
        FillBlankComponent fillBlankComponent = componentList.get(position);
        switch (fillBlankComponent.getType()) {
            case text:
                return TEXT_TYPE;
            case input:
                return INPUT_TYPE;
            case select:
                return SELECT_TYPE;
            default:
                throw new IllegalStateException("Illegal type of fill blank component");
        }
    }

    @Override
    public void onBindViewHolder(FillBlankViewHolderBase holder, int position) {
        FillBlankComponent fillBlankComponent = componentList.get(position);
        holder.bindData(fillBlankComponent);
        holder.makeEnabled(isAllEnabled);
    }

    @Override
    public int getItemCount() {
        return componentList.size();
    }

    public void setAllItemsEnabled(boolean isAllEnabled) {
        this.isAllEnabled = isAllEnabled;
        notifyItemRangeChanged(0, getItemCount());
    }

    abstract class FillBlankViewHolderBase extends RecyclerView.ViewHolder {

        public FillBlankViewHolderBase(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public abstract void bindData(FillBlankComponent fillBlankComponent);

        public final void makeEnabled(boolean isEnabled) {
            getRoot().setEnabled(isEnabled);
        }

        public abstract View getRoot();
    }

    private void changeAnswerAtPosition(int position, @Nullable CharSequence answer) {
        if (answer != null) {
            componentList.get(position).setDefaultValue(answer.toString());
        } else {
            componentList.get(position).setDefaultValue(null);
        }
    }

    class TextViewHolder extends FillBlankViewHolderBase {

        @BindView(R.id.latex_text_fill_blanks)
        ProgressLatexView progressLatexView;


        public TextViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bindData(FillBlankComponent fillBlankComponent) {
            progressLatexView.setAnyText(fillBlankComponent.getText());
        }

        @Override
        public View getRoot() {
            return progressLatexView;
        }
    }

    class SelectViewHolder extends FillBlankViewHolderBase {

        @BindView(R.id.spinner_fill_blanks)
        Spinner spinner;

        ArrayAdapter<String> dataAdapter;

        public SelectViewHolder(View itemView) {
            super(itemView);
            dataAdapter = new ArrayAdapter<>(itemView.getContext(), R.layout.stepik_spinner_item, new ArrayList<String>());
            NothingSelectedSpinnerAdapter wrappedAdapter = new NothingSelectedSpinnerAdapter(dataAdapter, R.layout.fill_blanks_prompt, itemView.getContext());
            dataAdapter.setDropDownViewResource(R.layout.stepik_spinner_dropdown_item);
            spinner.setAdapter(wrappedAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int adapterPosition = getAdapterPosition();
                    if (position > 0 && adapterPosition >= 0 && adapterPosition < componentList.size()) {
                        FillBlankComponent fillBlankComponent = componentList.get(adapterPosition);
                        String answer = fillBlankComponent.getOptions().get(position - 1);
                        changeAnswerAtPosition(adapterPosition, answer);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    changeAnswerAtPosition(getAdapterPosition(), null);
                }
            });
        }

        @Override
        public void bindData(FillBlankComponent fillBlankComponent) {
            Timber.d("Bind view, allocate some strings");
            List<String> rawOptions = fillBlankComponent.getOptions();
            if (rawOptions == null) {
                throw new IllegalArgumentException("options in select of fill blank cannot be null");
            }

            List<String> options = new ArrayList<>(rawOptions.size());
            final String defaultValue = fillBlankComponent.getDefaultValue();
            int indexForSelection = -1;
            for (int i = 0; i < rawOptions.size(); i++) {
                String item = rawOptions.get(i);
                options.add(item.trim());
                if (defaultValue != null && defaultValue.equals(item)) {
                    indexForSelection = i;
                }
            }

            dataAdapter.clear();
            dataAdapter.addAll(options);
            dataAdapter.notifyDataSetChanged();
            spinner.setSelection(indexForSelection + 1, false);
        }

        @Override
        public View getRoot() {
            return spinner;
        }
    }

    class InputViewHolder extends FillBlankViewHolderBase {

        @BindView(R.id.input_view_fill_blanks)
        EditText editText;

        public InputViewHolder(View itemView) {
            super(itemView);
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    changeAnswerAtPosition(getAdapterPosition(), s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

        @Override
        public void bindData(FillBlankComponent fillBlankComponent) {
            String defaultValue = fillBlankComponent.getDefaultValue();
            if (defaultValue != null) {
                editText.setText(defaultValue);
            }
        }

        @Override
        public View getRoot() {
            return editText;
        }
    }

}
