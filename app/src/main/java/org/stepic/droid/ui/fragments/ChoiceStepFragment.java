package org.stepic.droid.ui.fragments;

import android.os.Bundle;
import android.view.View;

import org.stepic.droid.R;
import org.stepik.android.model.learning.attempts.Attempt;
import org.stepic.droid.ui.adapters.StepikRadioGroupAdapter;
import org.stepic.droid.ui.custom.StepikRadioGroup;
import org.stepik.android.model.learning.Reply;

public class ChoiceStepFragment extends StepAttemptFragment {

    private StepikRadioGroupAdapter choiceAdapter;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        StepikRadioGroup choiceContainer = (StepikRadioGroup) getLayoutInflater().inflate(R.layout.view_choice_attempt, attemptContainer, false);
        attemptContainer.addView(choiceContainer);
        attemptContainer.setPadding(0, 0, 0, 0);

        choiceAdapter = new StepikRadioGroupAdapter(choiceContainer);
        choiceAdapter.setActionButton(actionButton);
    }

    /**
     * it is unique for each attempt type
     *
     * @param attempt data of task
     */
    @Override
    protected void showAttempt(Attempt attempt) {
        choiceAdapter.setAttempt(attempt);
    }


    //it is unique for each type
    @Override
    protected void blockUIBeforeSubmit(boolean needBlock) {
        choiceAdapter.setEnabled(!needBlock);
    }

    //it is unique for each type of replay
    @Override
    protected Reply generateReply() {
        return choiceAdapter.getReply();
    }


    @Override
    protected void onRestoreSubmission() {
        choiceAdapter.setSubmission(submission);
    }
}
