package org.stepic.droid.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.stepic.droid.R;
import org.stepik.android.model.learning.attempts.Attempt;
import org.stepic.droid.model.Reply;

public class StringStepFragment extends StepAttemptFragment {

    protected EditText answerField;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        answerField = (EditText) ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_free_answer_attempt, attemptContainer, false);
        attemptContainer.addView(answerField);
    }

    @Override
    protected final void showAttempt(Attempt attempt) {
        answerField.getText().clear();
    }

    @Override
    protected final void blockUIBeforeSubmit(boolean needBlock) {
        answerField.setEnabled(!needBlock);
    }

    @Override
    protected Reply generateReply() {
        return new Reply.Builder()
                .setText(answerField.getText().toString())
                .build();
    }

    @Override
    protected void onRestoreSubmission() {
        Reply reply = submission.getReply();
        if (reply == null) return;

        String text = reply.getText();
        answerField.setText(text);
    }

    @Override
    public void onPause() {
        super.onPause();
        answerField.clearFocus();
    }
}
