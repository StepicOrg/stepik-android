package org.stepic.droid.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import org.stepic.droid.R;
import org.stepik.android.model.learning.attempts.Attempt;
import org.stepic.droid.model.Reply;
import org.stepic.droid.ui.adapters.FillBlanksAdapter;
import org.stepik.android.model.learning.attempts.FillBlankComponent;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class FillBlanksFragment extends StepAttemptFragment {

    private RecyclerView recyclerContainer;
    private final List<FillBlankComponent> componentList = new ArrayList<>();
    FillBlanksAdapter fillBlanksAdapter;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View fillBlanksView = ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_fill_blanks, attemptContainer, false);
        recyclerContainer = ButterKnife.findById(fillBlanksView, R.id.recycler);
        recyclerContainer.setNestedScrollingEnabled(false);
        attemptContainer.addView(fillBlanksView);

        recyclerContainer.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    @Override
    protected void showAttempt(Attempt attempt) {
        componentList.clear();
        componentList.addAll(attempt.getDataset().getComponents());
        fillBlanksAdapter = new FillBlanksAdapter(componentList);
        recyclerContainer.setAdapter(fillBlanksAdapter);
    }

    @Override
    protected Reply generateReply() {
        if (fillBlanksAdapter == null) {
            throw new IllegalStateException("adapter cant be null on generating reply");
        }
        // TODO: 23.01.17 make checking is all values is not null?

        List<String> blanks = new ArrayList<>();
        for (FillBlankComponent item : componentList) {
            if (item.getType().canSubmit()) {
                String defaultValue = item.getDefaultValue();
                if (defaultValue == null) {
                    blanks.add("");
                } else {
                    blanks.add(defaultValue);
                }
            }
        }
        return new Reply.Builder()
                .setBlanks(blanks)
                .build();
    }

    @Override
    protected void blockUIBeforeSubmit(boolean needBlock) {
        if (fillBlanksAdapter != null) {
            fillBlanksAdapter.setAllItemsEnabled(!needBlock);
        }
    }

    @Override
    protected void onRestoreSubmission() {
        Reply reply = submission.getReply();

        List<String> blanksFromReply = reply.getBlanks();
        int i = 0;
        for (FillBlankComponent component : componentList) {
            if (component.getType().canSubmit() && i < blanksFromReply.size()) {
                component.setDefaultValue(blanksFromReply.get(i));
                i++;
            }
        }

        fillBlanksAdapter.notifyDataSetChanged();
    }

}
