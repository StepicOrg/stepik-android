package org.stepic.droid.view.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.events.comments.NewCommentWasAdded;
import org.stepic.droid.events.steps.StepWasUpdatedEvent;
import org.stepic.droid.model.Attempt;

public abstract class SingleLineSendStepFragment extends StepWithAttemptsFragment {

    protected EditText mAnswerField;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mAnswerField = (EditText) ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_single_line_attempt, mAttemptContainer, false);
        mAttemptContainer.addView(mAnswerField);
        mAnswerField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    mActionButton.performClick();
                    handled = true;
                }
                return handled;
            }
        });
        return v;
    }


    @Override
    protected final void showAttempt(Attempt attempt) {
        mAnswerField.getText().clear();
    }

    @Override
    protected final void blockUIBeforeSubmit(boolean needBlock) {
        mAnswerField.setEnabled(!needBlock);
    }

    @Subscribe
    public void onNewCommentWasAdded(NewCommentWasAdded event) {
        super.onNewCommentWasAdded(event);

    }

    @Subscribe
    public void onStepWasUpdated(StepWasUpdatedEvent event) {
        super.onStepWasUpdated(event);
    }
}
