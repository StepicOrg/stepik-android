package org.stepic.droid.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepik.android.model.learning.attempts.Attempt;

public abstract class SingleLineSendStepFragment extends StepAttemptFragment {

    protected EditText answerField;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        answerField = (EditText) ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_single_line_attempt, attemptContainer, false);
        attemptContainer.addView(answerField);
        answerField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    actionButton.performClick();
                    handled = true;
                }
                return handled;
            }
        });
    }

    @Override
    protected final void showAttempt(Attempt attempt) {
        answerField.getText().clear();
    }

    @Override
    protected final void blockUIBeforeSubmit(boolean needBlock) {
        answerField.setEnabled(!needBlock);
    }

}
