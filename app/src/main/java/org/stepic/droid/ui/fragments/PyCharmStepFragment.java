package org.stepic.droid.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.stepic.droid.R;
import org.stepik.android.model.attempts.Attempt;
import org.stepik.android.model.Reply;

import butterknife.BindString;

public class PyCharmStepFragment extends StepAttemptFragment {

    private TextView messageField;

    @BindString(R.string.py_message)
    String pyMessage;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        messageField = (TextView) ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_py_step, attemptContainer, false);
        messageField.setMovementMethod(LinkMovementMethod.getInstance());
        attemptContainer.addView(messageField);
        actionButton.setVisibility(View.GONE);
    }

    @Override
    protected void showAttempt(Attempt attempt) {
        messageField.setText(getTextResolver().fromHtml(pyMessage));
    }

    @Override
    protected Reply generateReply() {
        return new Reply();
    }

    @Override
    protected void blockUIBeforeSubmit(boolean needBlock) {
        messageField.setEnabled(true);
    }

    @Override
    protected void onRestoreSubmission() {
        messageField.setText(getTextResolver().fromHtml(pyMessage));
    }

}
