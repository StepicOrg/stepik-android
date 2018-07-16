package org.stepic.droid.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.stepic.droid.R;
import org.stepik.android.model.learning.attempts.Attempt;
import org.stepik.android.model.learning.attempts.Dataset;
import org.stepic.droid.model.Reply;
import org.stepik.android.model.learning.reply.TableChoiceAnswer;
import org.stepic.droid.ui.adapters.TableChoiceAdapter;
import org.stepic.droid.ui.decorators.GridDividerItemDecoration;
import org.stepic.droid.util.DpPixelsHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class TableChoiceStepFragment extends StepAttemptFragment {

    public static TableChoiceStepFragment newInstance() {
        return new TableChoiceStepFragment();
    }

    private RecyclerView recyclerContainer;
    private List<TableChoiceAnswer> answerList;

    @Nullable
    GridLayoutManager gridLayoutManager;

    TableChoiceAdapter adapter;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View tableQuizView = getLayoutInflater().inflate(R.layout.view_table_quiz_layout, attemptContainer, false);
        int dp8inPx = (int) DpPixelsHelper.convertDpToPixel(8f);
        attemptContainer.setPadding(0, dp8inPx, 0, dp8inPx);
        recyclerContainer = ButterKnife.findById(tableQuizView, R.id.recycler);
        recyclerContainer.setNestedScrollingEnabled(false);
        recyclerContainer.addItemDecoration(new GridDividerItemDecoration(getContext()));
        attemptContainer.addView(tableQuizView);
    }

    @Override
    protected void showAttempt(Attempt attempt) {
        Dataset dataset = attempt.getDataset();
        List<String> rows = dataset.getRows();
        List<String> columns = dataset.getColumns();
        String description = dataset.getDescription();
        boolean isCheckbox = dataset.isCheckbox();

        answerList = initAnswerListFromAttempt(rows, columns);

        gridLayoutManager = new GridLayoutManager(getContext(), rows.size() + 1, GridLayoutManager.HORIZONTAL, false);
        adapter = new TableChoiceAdapter(getActivity(), rows, columns, description, isCheckbox, answerList);
        recyclerContainer.setLayoutManager(gridLayoutManager);
        recyclerContainer.setAdapter(adapter);
    }

    private ArrayList<TableChoiceAnswer> initAnswerListFromAttempt(List<String> rows, List<String> columns) {
        // may be we should do it on background thread, but then we should rewrite logic of parent class, and do same actions for each quiz on background thread
        ArrayList<TableChoiceAnswer> result = new ArrayList<>(rows.size());
        for (String nameRow : rows) {
            List<TableChoiceAnswer.Cell> oneRowAnswer = new ArrayList<>(columns.size());
            //we should create new objects for each try –> it is generated in for cycle (but Strings is same objects)
            for (String nameColumn : columns) {
                oneRowAnswer.add(new TableChoiceAnswer.Cell(nameColumn, false));
            }
            result.add(new TableChoiceAnswer(nameRow, oneRowAnswer));
        }

        return result;
    }

    @Override
    protected Reply generateReply() {
        return new Reply.Builder()
                .setTableChoices(answerList)
                .build();
    }

    @Override
    protected void blockUIBeforeSubmit(boolean needBlock) {
        if (adapter != null) {
            adapter.setAllItemsEnabled(!needBlock);
        }
    }

    @Override
    protected void onRestoreSubmission() {
        Reply reply = submission.getReply();
        if (reply == null) return;

        List<TableChoiceAnswer> choices = reply.getTableChoices();
        if (choices == null) return;

        answerList.clear();
        answerList.addAll(choices);

        adapter.notifyDataSetChanged();
    }

}