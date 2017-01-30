package org.stepic.droid.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.events.InternetIsEnabledEvent;
import org.stepic.droid.events.comments.NewCommentWasAddedOrUpdateEvent;
import org.stepic.droid.events.steps.StepWasUpdatedEvent;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Dataset;
import org.stepic.droid.model.Reply;
import org.stepic.droid.model.TableChoiceAnswer;
import org.stepic.droid.ui.adapters.TableQuizAdapter;
import org.stepic.droid.util.DpPixelsHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class TableStepFragment extends StepAttemptFragment {

    public static TableStepFragment newInstance() {
        return new TableStepFragment();
    }

    private RecyclerView recyclerContainer;
    private List<TableChoiceAnswer> answerList;

    @Nullable
    GridLayoutManager gridLayoutManager;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View tableQuizView = ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_table_quiz_layout, attemptContainer, false);
        int dp8inPx = (int) DpPixelsHelper.convertDpToPixel(8f);
        attemptContainer.setPadding(0, dp8inPx, 0, dp8inPx);
        recyclerContainer = ButterKnife.findById(tableQuizView, R.id.recycler);
        recyclerContainer.setNestedScrollingEnabled(false);
        attemptContainer.addView(tableQuizView);
    }

    @Override
    protected void showAttempt(Attempt attempt) {
        Dataset dataset = attempt.getDataset();
        List<String> rows = dataset.getTableRows();
        List<String> columns = dataset.getTableColumns();
        String description = dataset.getDescriptionTableQuiz();
        boolean isCheckbox = dataset.isTableCheckbox();

        answerList = initAnswerListFromAttempt(rows, columns);

        gridLayoutManager = new GridLayoutManager(getContext(), rows.size() + 1, GridLayoutManager.HORIZONTAL, false);
        RecyclerView.Adapter adapter = new TableQuizAdapter(rows, columns, description, isCheckbox, answerList);
        recyclerContainer.setLayoutManager(gridLayoutManager);
        recyclerContainer.setAdapter(adapter);
    }

    private ArrayList<TableChoiceAnswer> initAnswerListFromAttempt(List<String> rows, List<String> columns) {
        // may be we should do it on background thread, but then we should rewrite logic of parent class, and do same actions for each quiz on background thread
        ArrayList<TableChoiceAnswer> result = new ArrayList<>(rows.size());
        for (String nameRow : rows) {
            List<TableChoiceAnswer.Companion.Cell> oneRowAnswer = new ArrayList<>(columns.size());
            //we should create new objects for each try –> it is generated in for cycle (but Strings is same objects)
            for (String nameColumn : columns) {
                oneRowAnswer.add(new TableChoiceAnswer.Companion.Cell(nameColumn, false));
            }
            result.add(new TableChoiceAnswer(nameRow, oneRowAnswer));
        }

        return result;
    }

    @Override
    protected Reply generateReply() {
        //todo get from user answer (some list)

        //stub:
        List<TableChoiceAnswer> choiceAnswerList = new ArrayList<>();
        List<TableChoiceAnswer.Companion.Cell> cellsInFirstRow = new ArrayList<>();
        cellsInFirstRow.add(new TableChoiceAnswer.Companion.Cell("One", true));
        TableChoiceAnswer tableChoiceAnswer = new TableChoiceAnswer("Name of row", cellsInFirstRow);
        choiceAnswerList.add(tableChoiceAnswer);

        return new Reply.Builder()
                .setChoices(choiceAnswerList)
                .build();
    }

    @Override
    protected void blockUIBeforeSubmit(boolean needBlock) {
        //todo block elements in list
    }

    @Override
    protected void onRestoreSubmission() {
        //todo notify quiz changed
    }

    @Subscribe
    @Override
    public void onInternetEnabled(InternetIsEnabledEvent enabledEvent) {
        super.onInternetEnabled(enabledEvent);
    }

    @Subscribe
    public void onNewCommentWasAdded(NewCommentWasAddedOrUpdateEvent event) {
        super.onNewCommentWasAdded(event);

    }

    @Subscribe
    public void onStepWasUpdated(StepWasUpdatedEvent event) {
        super.onStepWasUpdated(event);
    }
}
