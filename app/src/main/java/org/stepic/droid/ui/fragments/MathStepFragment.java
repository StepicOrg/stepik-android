package org.stepic.droid.ui.fragments;

import org.stepic.droid.model.Reply;

public class MathStepFragment extends SingleLineSendStepFragment {

    @Override
    protected Reply generateReply() {
        return new Reply.Builder()
                .setFormula(answerField.getText().toString())
                .build();
    }

    @Override
    protected void onRestoreSubmission() {
        Reply reply = submission.getReply();
        if (reply == null) return;

        String text = reply.getFormula();
        answerField.setText(text);
    }

}
