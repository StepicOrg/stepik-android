package org.stepic.droid.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.View;

import org.stepik.android.model.learning.replies.Reply;

public class NumberStepFragment extends SingleLineSendStepFragment {

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        answerField.setRawInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    @Override
    protected Reply generateReply() {
        return new Reply.Builder()
                .setNumber(answerField.getText().toString())
                .build();
    }

    @Override
    protected void onRestoreSubmission() {
        Reply reply = submission.getReply();
        if (reply == null) return;

        String text = reply.getNumber();
        answerField.setText(text);
    }

}
