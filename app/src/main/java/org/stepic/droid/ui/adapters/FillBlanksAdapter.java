package org.stepic.droid.ui.adapters;

import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.R;
import org.stepic.droid.model.FillBlankComponent;
import org.stepic.droid.ui.custom.ProgressLatexView;
import org.stepic.droid.ui.util.NothingSelectedSpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FillBlanksAdapter extends RecyclerView.Adapter<FillBlanksAdapter.FillBlankViewHolderBase> {
    final static int TEXT_TYPE = 0;
    final static int INPUT_TYPE = 1;
    final static int SELECT_TYPE = 2;

    private List<FillBlankComponent> componentList;

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
    }

    @Override
    public int getItemCount() {
        return componentList.size();
    }

    static abstract class FillBlankViewHolderBase extends RecyclerView.ViewHolder {

        public FillBlankViewHolderBase(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public abstract void bindData(FillBlankComponent fillBlankComponent);
    }


    static class TextViewHolder extends FillBlankViewHolderBase {

        @BindView(R.id.latex_text_fill_blanks)
        ProgressLatexView progressLatexView;


        public TextViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bindData(FillBlankComponent fillBlankComponent) {
            progressLatexView.setPlainOrLaTeXText(fillBlankComponent.getText()); //// FIXME: 23.01.17 HTML is allowed here
        }
    }

    static class SelectViewHolder extends FillBlankViewHolderBase {

        @BindView(R.id.spinner_fill_blanks)
        AppCompatSpinner spinner;

        ArrayAdapter<String> dataAdapter;

        public SelectViewHolder(View itemView) {
            super(itemView);
            dataAdapter = new ArrayAdapter<>(itemView.getContext(), android.R.layout.simple_spinner_item, new ArrayList<String>());
            NothingSelectedSpinnerAdapter wrappedAdapter = new NothingSelectedSpinnerAdapter(dataAdapter, R.layout.fill_blanks_prompt, itemView.getContext());
            spinner.setAdapter(wrappedAdapter);
        }

        @Override
        public void bindData(FillBlankComponent fillBlankComponent) {
            List<String> options = fillBlankComponent.getOptions();
            if (options == null) {
                throw new IllegalArgumentException("options in select of fill blank cannot be null");
            }
            dataAdapter.clear();
            dataAdapter.addAll(options);
            dataAdapter.notifyDataSetChanged();
        }
    }

    static class InputViewHolder extends FillBlankViewHolderBase {

        @BindView(R.id.input_view_fill_blanks)
        EditText editText;

        public InputViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bindData(FillBlankComponent fillBlankComponent) {
            //// TODO: 23.01.17 we should restore data
        }
    }

}
