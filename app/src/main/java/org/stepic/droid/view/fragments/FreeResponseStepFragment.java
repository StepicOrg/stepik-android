package org.stepic.droid.view.fragments;

import org.stepic.droid.R;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Reply;

import butterknife.BindString;

public class FreeResponseStepFragment extends StepWithAttemptsFragment {

    @BindString(R.string.correct_free_response)
    String mCorrectString;

    @Override
    protected void showAttempt(Attempt attempt) {

    }

    @Override
    protected Reply generateReply() {
        return null;
    }

    @Override
    protected void blockUIBeforeSubmit(boolean needBlock) {

    }

    @Override
    protected void onRestoreSubmission() {

    }

    @Override
    protected String getCorrectString() {
        return mCorrectString;
    }
}
